package com.example.forum.auth;

import com.example.forum.dto.request.LogoutRequest;
import com.example.forum.dto.request.ResetPasswordRequest;
import com.example.forum.dto.response.AuthenticationResponse;
import com.example.forum.dto.response.UserDeviceResponse;
import com.example.forum.dto.response.UserSummaryDto;
import com.example.forum.dto.response.VerifyOtpResponse;
import com.example.forum.entity.Enum.DeviceStatus;
import com.example.forum.entity.UserDevice;
import com.example.forum.exception.EmailAlreadyExistsException;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.repository.UserDeviceRepository;
import com.example.forum.security.JWTService;
import com.example.forum.dto.request.AuthenticationRequest;
import com.example.forum.dto.request.RegisterRequest;
import com.example.forum.entity.Role;
import com.example.forum.entity.UserEntity;
import com.example.forum.repository.RoleRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.security.TokenUtils;
import com.example.forum.service.RedisService;
import com.example.forum.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final AuthenticationManager authManager;
    private final VerificationService verificationService;
    private final RoleRepository roleRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final RedisService redisService;
    private final LoginAttemptService loginAttemptService;
    private static final String PREFIX_RESET_TOKEN="password_reset_token:";
    private static final String REVOKED_USER_KEY = "revoked_user:";


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

    public VerifyOtpResponse verifyCode(String email, String code) {
        return verificationService.verifyToken(email, code);
    }



    public AuthenticationResponse authenticate(AuthenticationRequest request, String userAgent, String ip) {
        if (loginAttemptService.isLocked(request.getEmail())){
            throw new ResponseStatusException(HttpStatus.LOCKED, "Your account is locked for 15 minutes because of over 5 attempts to enter correct password");
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
        String deviceId = request.getDeviceId();
        if (deviceId == null) deviceId = UUID.randomUUID().toString();
        String rawRefreshToken = UUID.randomUUID().toString();

        boolean isNewDevice =saveUserDevice(user, deviceId, rawRefreshToken, userAgent, ip);
        if (isNewDevice){
            // logic thông báo
            System.out.printf("CẢNH BÁO: Phát hiện đăng nhập từ thiết bị mới! User: {}, IP: {}", user.getEmail(), ip);
        }

        var jwtAccessToken = jwtService.generateAccessToken(user, deviceId);
        return AuthenticationResponse.builder()
                .accessToken(jwtAccessToken)
                .refreshToken(rawRefreshToken)
                .deviceId(deviceId)               // [QUAN TRỌNG] Trả lại deviceId để client lưu
                .user(UserSummaryDto.builder()
                        .userId(user.getUserId())
                        .username(user.displayUsername())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .build();

    }
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
        userDevice.setDeviceName(ua);
        userDevice.setDeviceName(ua); // Tạm thời lưu user-agent, sau này parse đẹp sau
        userDevice.setLastIp(ip);
        userDevice.setStatus(DeviceStatus.ACTIVE);
        userDevice.setLastActiveAt(Instant.now());
        userDevice.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        userDeviceRepository.save(userDevice);

        return isNewDevice;
    }

    public AuthenticationResponse refreshToken(String rawRefreshToken) {
        if (rawRefreshToken== null || rawRefreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh Token is missing!");
        }
        String hashedInputToken = TokenUtils.hashToken(rawRefreshToken);

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


    public void logout(LogoutRequest request, String accessToken){
        String refreshToken = request.getRefreshToken();

        if(refreshToken==null || refreshToken.isBlank()){
            return;
        }

        String hash = TokenUtils.hashToken(refreshToken);

        var userDeviceOptional= userDeviceRepository.findByRefreshTokenHash(hash);

        if(userDeviceOptional.isPresent()) {
            UserDevice userDevice = userDeviceOptional.get();
            userDevice.setStatus(DeviceStatus.REVOKED);
            userDeviceRepository.save(userDevice);
            // device.setRefreshTokenHash(null);
        }

        if (accessToken != null){
            long exprirationTime = jwtService.extractExpiration(accessToken).getTime();
            long currentTime = System.currentTimeMillis();
            long remainTime =exprirationTime-currentTime;

            if (remainTime >0) {
                redisService.set("BL_"+ accessToken, "logout", remainTime, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void logoutA(UserEntity user){

    }

    public void forgotPassword(String email){
        System.out.println("Email nhận được: '" + email + "'");
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));
        System.out.println("forgot - authservice2");
        verificationService.sendVerificationEmail(user);
    }


    public void resetPassword(ResetPasswordRequest request){
        String resetTokenKey = PREFIX_RESET_TOKEN+request.getResetToken();
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        Object storedEmail = redisService.get(resetTokenKey);

        if (storedEmail != null && storedEmail.toString().equals(request.getEmail())) {
            user.setUserPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            redisService.delete(resetTokenKey);
        } else {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }
    }

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

    private static final String REVOKED_DEVICE_KEY = "revoked_device:";
    public void revokeDevice(UserEntity user, String deviceTargetId){
        UserDevice device = userDeviceRepository.findByUserIdAndDeviceId(user.getUserId(), deviceTargetId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        device.setStatus(DeviceStatus.REVOKED);
        userDeviceRepository.save(device);

        long revocationTime = System.currentTimeMillis();

        long accessTokenTTL = 1000; // vi access token la 900

        redisService.set(
                REVOKED_DEVICE_KEY+deviceTargetId,
                revocationTime,
                accessTokenTTL,
                TimeUnit.SECONDS
        );
    }

    public void revokeAllDevice(UserEntity currentUser){
        userDeviceRepository.revokeAllByUserId(currentUser.getUserId());
        long revocationTime = System.currentTimeMillis();
        long accessTokenTTL = 3600;

        redisService.set(
                REVOKED_USER_KEY +currentUser.getUserId(),
                String.valueOf(revocationTime),
                accessTokenTTL,
                TimeUnit.SECONDS
        );
    }

}
