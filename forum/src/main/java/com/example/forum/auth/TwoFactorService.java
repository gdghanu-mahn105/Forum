package com.example.forum.auth;

import com.example.forum.dto.response.TwoFactorResponse;
import com.example.forum.entity.UserEntity;
import com.example.forum.exception.OtpVerificationException;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.repository.BackupCodeRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.security.SecurityService;
import com.example.forum.service.RedisService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TwoFactorService {
    private final GoogleAuthenticator googleAuthenticator;
    private final UserRepository userRepository;
    private final RedisService redisService;
    private static final String PREFIX_TEMPT_2AF = "tempt:2af:";
    private final BackupCodeService backupCodeService;
    private final BackupCodeRepository backupCodeRepository;
    private final SecurityService securityService;

    public TwoFactorService(UserRepository userRepository,
                            RedisService redisService,
                            BackupCodeService backupCodeService,
                            BackupCodeRepository backupCodeRepository,
                            SecurityService securityService){
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setTimeStepSizeInMillis(30000)
                .setWindowSize(1)
                .setCodeDigits(6)
                .build();
        this.googleAuthenticator = new GoogleAuthenticator(config);
        this.userRepository= userRepository;
        this.redisService=redisService;
        this.backupCodeService=backupCodeService;
        this.backupCodeRepository=backupCodeRepository;
        this.securityService=securityService;
    }

    public String generateNewSecret(){
        return googleAuthenticator.createCredentials().getKey();
    }

    public String generateQrCodeUri(String secret, String email){
        return String.format("otpauth://totp/MyForum:%s?secret=%s&issuer=MyForum", email, secret);
    }

    public boolean isOtpValid(String secret, int code) {
        return googleAuthenticator.authorize(secret, code);
    }

    public TwoFactorResponse enableTwoFactor(String email){
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        String secret = generateNewSecret();
        String qrUrl = generateQrCodeUri(secret, email);
        redisService.set(PREFIX_TEMPT_2AF+email, secret, 900, TimeUnit.SECONDS);

        return TwoFactorResponse.builder()
                .secret(secret)
                .qrUrl(qrUrl)
                .build();
    }

    public List<String> verifyOtp(String email, int otp){
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        Object storedSecret = redisService.get(PREFIX_TEMPT_2AF + email);
        if (storedSecret == null) {
            throw new ResourceNotFoundException("Your code is expired or unavailable. Please retake enable/ setup step.");
        }

        String secretStr = storedSecret.toString();
        boolean result = isOtpValid(secretStr,otp);
        if (!result) {
            throw new OtpVerificationException("Invalid OTP");
        }
        if(!user.isTwoFactorEnabled()){
            user.setTwoFactorEnabled(true);
            user.setTwoFactorSecret(secretStr);
            userRepository.save(user);
        }
        redisService.delete(PREFIX_TEMPT_2AF + email);


        return backupCodeService.generateBackupCode(user);

    }

    @Transactional
    public void disable2fa(UserEntity user, String password){

        securityService.validatePassword(user, password);

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);
        backupCodeRepository.deleteByUserEntityUserId(user.getUserId());
    }

}
