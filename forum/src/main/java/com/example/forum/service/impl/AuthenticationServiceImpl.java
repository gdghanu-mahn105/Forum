package com.example.forum.service.impl;

import com.example.forum.constant.AppConstants;
import com.example.forum.dto.request.LogoutRequest;
import com.example.forum.dto.request.ResetPasswordRequest;
import com.example.forum.dto.response.AuthenticationResponse;
import com.example.forum.dto.response.UserDeviceResponse;
import com.example.forum.dto.response.UserSummaryDto;
import com.example.forum.dto.response.VerifyOtpResponse;
import com.example.forum.entity.Enum.DeviceStatus;
import com.example.forum.entity.UserDevice;
import com.example.forum.exception.EmailAlreadyExistsException;
import com.example.forum.exception.OtpVerificationException;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.repository.UserDeviceRepository;
import com.example.forum.security.jwt.JWTService;
import com.example.forum.dto.request.AuthenticationRequest;
import com.example.forum.dto.request.RegisterRequest;
import com.example.forum.entity.Role;
import com.example.forum.entity.UserEntity;
import com.example.forum.repository.RoleRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.security.jwt.TokenUtils;
import com.example.forum.service.*;
import com.example.forum.utils.RequestUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserDeviceRepository userDeviceRepository;

    private final VerificationService verificationService;
    private final LoginAttemptService loginAttemptService;
    private final TwoFactorService twoFactorService;
    private final BackupCodeService backupCodeService;
    private final EmailService emailService;
    private final CacheService cacheService;

    private final AuthenticationManager authManager;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    @Value("${app.refresh-token.expiration}")
    private long refreshTokenExpirationTime;

    @Value("${app.redis.revoke.user.timeout}")
    private long revokedByUserTimeout;


    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @Override
    public UserSummaryDto register(RegisterRequest request) {

        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException( "Email was used");
        }

        if(request.getPassword()==null){
            throw new IllegalArgumentException("Password must be filled");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(()-> new RuntimeException("role not found"));


        var user= UserEntity.builder()
                .userName(request.getUserName())
                .email(request.getEmail())
                .userPassword(passwordEncoder.encode(request.getPassword()))
                .avatarUrl("https://cdn-icons-png.flaticon.com/512/9815/9815472.png") // default avatar url
                .roles(Set.of(userRole))
                .isVerified(false)
                .build();
        userRepository.save(user);

        verificationService.sendVerificationEmail(user);

        return new UserSummaryDto(user.getUserId(),
                user.displayUsername(),
                user.getEmail(),
                user.getAvatarUrl());
    }

    @Override
    public VerifyOtpResponse verifyCode(String email, String code) {
        return verificationService.verifyToken(email, code);
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        if(loginAttemptService.isLocked(request.getEmail())){
            throw new ResponseStatusException(HttpStatus.LOCKED, "Account is locked due to too many failed attempts. Please try again later.");
        }

        var user = userRepository.findByEmail(request.getEmail())
                 .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));


        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            loginAttemptService.loginSucceeded(request.getEmail());
        } catch (BadCredentialsException ex) {
            loginAttemptService.loginFail(request.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password!");
        } catch (DisabledException ex){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Your account is not verified");
        } catch (LockedException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is locked");
        }

        if (user.isTwoFactorEnabled()) {
            return AuthenticationResponse.builder()
                    .requiresTwoFactor(true)
                    .message("Two-factor authentication required")
                    .build();
        }
        return finalizeLogin(user, request.getDeviceId());
    }

    @Override
    public AuthenticationResponse verifyTwoFactorLogin(String email, String otpCode, String deviceId){
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isTwoFactorEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "2FA is not enabled for this user");
        }

        boolean isValid = false;
        if(otpCode.matches("\\d{6}")){
            int code = Integer.parseInt(otpCode);
            isValid = twoFactorService.isOtpValid(user.getTwoFactorSecret(), code);
        }

        if(!isValid){
            isValid= backupCodeService.verifyBackupCode(user, otpCode);
        }

        if (!isValid) {
            throw new OtpVerificationException("Invalid OTP Code");
        }

        return finalizeLogin(user, deviceId);
    }

    @Override
    public AuthenticationResponse finalizeLogin(UserEntity user, String deviceId){

        String ip= RequestUtils.getClientIp();
        String userAgent = RequestUtils.getUserAgent();

        if (deviceId == null) deviceId = UUID.randomUUID().toString();
        String rawRefreshToken = UUID.randomUUID().toString();

        boolean isNewDevice =saveUserDevice(user, deviceId, rawRefreshToken, userAgent, ip);
        if (isNewDevice){
            String emailBody = String.format(
                    "We noticed a new login to your account.\n\n" +
                            "- Device: %s\n" +
                            "- IP Address: %s\n" +
                            "- Time: %s\n\n" +
                            "If this wasn't you, please change your password immediately.",
                    userAgent,ip, formatter.format(Instant.now())
            );

            emailService.sendMail(user.getEmail(), "Security Alert: New Device Login", emailBody);
        }

        var jwtAccessToken = jwtService.generateAccessToken(user, deviceId);
        return AuthenticationResponse.builder()
                .accessToken(jwtAccessToken)
                .refreshToken(rawRefreshToken)
                .deviceId(deviceId)
                .user(UserSummaryDto.builder()
                        .userId(user.getUserId())
                        .username(user.displayUsername())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .build();

    }

    @Override
    public boolean saveUserDevice(UserEntity user, String deviceId, String rawRefreshToken, String ua, String ip){

        var existingDevice = userDeviceRepository.findByUserIdAndDeviceId(user.getUserId(), deviceId);
        boolean isNewDevice = existingDevice.isEmpty();

        UserDevice userDevice = existingDevice.orElse(
                UserDevice.builder()
                        .userId(user.getUserId())
                        .deviceId(deviceId)
                        .build());

        if (ua != null && ua.length() > 255) {
            ua = ua.substring(0, 255);
        }
        userDevice.setRefreshTokenHash(TokenUtils.hashToken(rawRefreshToken));
        userDevice.setDeviceName(ua); // Tạm thời lưu user-agent, sau này parse đẹp sau
        userDevice.setLastIp(ip);
        userDevice.setStatus(DeviceStatus.ACTIVE);
        userDevice.setLastActiveAt(Instant.now());
        userDevice.setExpiresAt(Instant.now().plus(refreshTokenExpirationTime, ChronoUnit.MILLIS));
        userDeviceRepository.save(userDevice);

        return isNewDevice;
    }

    @Override
    public AuthenticationResponse refreshToken(String rawRefreshToken) {



        if (rawRefreshToken== null || rawRefreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh Token is missing!");
        }
        String hashedInputToken = TokenUtils.hashToken(rawRefreshToken);

        String redisKey = AppConstants.PREFIX_BLACKLIST_REFRESH_TOKEN + hashedInputToken;
        if (cacheService.hasKey(redisKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh Token has been revoked (Logout)");
        }

        UserDevice userDevice = userDeviceRepository.findByRefreshTokenHash(hashedInputToken)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.FORBIDDEN,"Invalid Refresh Token"));

        if(userDevice.getExpiresAt().isBefore(Instant.now())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh Token Expired");
        }

        if (userDevice.getStatus() != DeviceStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token Revoked");
        }

        UserEntity user = userRepository.findById(userDevice.getUserId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userDevice.setLastActiveAt(Instant.now());
        userDeviceRepository.save(userDevice);

        String newAccessToken = jwtService.generateAccessToken(user, userDevice.getDeviceId());

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(rawRefreshToken)
                .deviceId(userDevice.getDeviceId())
                .user(new UserSummaryDto(
                        user.getUserId(),
                        user.displayUsername(),
                        user.getEmail(),
                        user.getAvatarUrl()))
                .build();
    }

    @Override
    public void logout(LogoutRequest request, String accessToken){
        String refreshToken = request.getRefreshToken();

        if(refreshToken==null || refreshToken.isBlank()){
            return;
        }

        String refreshTokenHashed = TokenUtils.hashToken(refreshToken);

        var userDeviceOptional= userDeviceRepository.findByRefreshTokenHash(refreshTokenHashed);



        if(userDeviceOptional.isPresent()) {
            UserDevice userDevice = userDeviceOptional.get();
            userDevice.setStatus(DeviceStatus.REVOKED);
            userDeviceRepository.save(userDevice);

            long refreshTokenRemainTime = 0;
            if(userDevice.getExpiresAt().isAfter(Instant.now())){
                refreshTokenRemainTime = userDevice.getExpiresAt().toEpochMilli() - System.currentTimeMillis();
            }

            if(refreshTokenRemainTime > 0) {
                String redisKey = AppConstants.PREFIX_BLACKLIST_REFRESH_TOKEN + refreshTokenHashed;
                cacheService.set(redisKey, "revoked", refreshTokenRemainTime, TimeUnit.MILLISECONDS);
            }
        }

        if (accessToken != null){
            long exprirationTime = jwtService.extractExpiration(accessToken).getTime();
            long currentTime = System.currentTimeMillis();
            long remainTime =exprirationTime-currentTime;

            if (remainTime >0) {
                cacheService.set(AppConstants.BLACKLIST_KEY+ accessToken, "logout", remainTime, TimeUnit.MILLISECONDS);
            }
        }
    }

    @Override
    public void forgotPassword(String email){
        System.out.println("Email nhận được: '" + email + "'");
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));
        System.out.println("forgot - authservice2");
        verificationService.sendVerificationEmail(user);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request){
        String resetTokenKey = AppConstants.PREFIX_RESET_TOKEN+request.getResetToken();
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        Object storedEmail = cacheService.get(resetTokenKey);

        if (storedEmail != null && storedEmail.toString().equals(request.getEmail())) {
            user.setUserPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            cacheService.delete(resetTokenKey);
        } else {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }
    }

    @Override
    public List<UserDeviceResponse> getAllDevice(UserEntity user, String currentUserDeviceId){
        List<UserDevice> userDeviceList = userDeviceRepository.findAllByUserIdAndStatus(user.getUserId(), DeviceStatus.ACTIVE);
        return userDeviceList.stream().map(userDevice -> UserDeviceResponse.builder()
                    .deviceId(userDevice.getDeviceId())
                    .deviceName(userDevice.getDeviceName())
                    .lastIp(userDevice.getLastIp())
                    .lastActiveAt(userDevice.getLastActiveAt())
                    .isCurrentDevice(userDevice.getDeviceId().equals(currentUserDeviceId))
                    .build()
        ).collect(Collectors.toList());
    }

    @Override
    public void revokeDevice(UserEntity user, String deviceTargetId){
        UserDevice device = userDeviceRepository.findByUserIdAndDeviceId(user.getUserId(), deviceTargetId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        device.setStatus(DeviceStatus.REVOKED);
        userDeviceRepository.save(device);

        long revocationTime = System.currentTimeMillis();

        cacheService.set(
                AppConstants.REVOKED_DEVICE_KEY+deviceTargetId,
                revocationTime,
                revokedByUserTimeout, //1000 vi access token la 900
                TimeUnit.SECONDS
        );
    }

    @Transactional
    @Override
    public void revokeAllDevice(UserEntity currentUser){
        userDeviceRepository.revokeAllByUserId(currentUser.getUserId());
        long revocationTime = System.currentTimeMillis();

        cacheService.set(
                AppConstants.REVOKED_USER_KEY +currentUser.getUserId(),
                String.valueOf(revocationTime),
                revokedByUserTimeout,
                TimeUnit.SECONDS
        );
    }



}
