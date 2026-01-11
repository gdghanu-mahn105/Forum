package com.example.forum.service.impl;

import com.example.forum.constant.AppConstants;
import com.example.forum.constant.MessageConstants;
import com.example.forum.dto.response.TwoFactorResponse;
import com.example.forum.entity.UserEntity;
import com.example.forum.exception.OtpVerificationException;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.repository.BackupCodeRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.service.BackupCodeService;
import com.example.forum.service.CacheService;
import com.example.forum.service.TwoFactorService;
import com.example.forum.utils.SecurityUtils;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TwoFactorServiceImpl implements TwoFactorService {
    private final GoogleAuthenticator googleAuthenticator;
    private final SecurityUtils securityService;
    private final CacheService redisService;
    private final BackupCodeService backupCodeService;

    private final BackupCodeRepository backupCodeRepository;
    private final UserRepository userRepository;

    @Value("${app.2fa.secret.timeout}")
    private long setup2faTimeout;

    public TwoFactorServiceImpl(UserRepository userRepository,
                                RedisService redisService,
                                BackupCodeServiceImpl backupCodeService,
                                BackupCodeRepository backupCodeRepository,
                                SecurityUtils securityService){
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

    @Override
    public String generateNewSecret(){
        return googleAuthenticator.createCredentials().getKey();
    }

    @Override
    public String generateQrCodeUri(String secret, String email){
        return String.format("otpauth://totp/MyForum:%s?secret=%s&issuer=MyForum", email, secret);
    }

    @Override
    public boolean isOtpValid(String secret, int code) {
        return googleAuthenticator.authorize(secret, code);
    }

    @Override
    public TwoFactorResponse enableTwoFactor(String email){
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        String secret = generateNewSecret();
        String qrUrl = generateQrCodeUri(secret, email);
        redisService.set(AppConstants.PREFIX_TEMP_2FA+email, secret,setup2faTimeout, TimeUnit.SECONDS);

        return TwoFactorResponse.builder()
                .secret(secret)
                .qrUrl(qrUrl)
                .build();
    }

    @Override
    public List<String> verifyOtp(String email, int otp){
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        String keyTemp2fa = AppConstants.PREFIX_TEMP_2FA + email;

        Object storedSecret = redisService.get(keyTemp2fa);
        if (storedSecret == null) {
            throw new ResourceNotFoundException(MessageConstants.CODE_2FA_EXPIRED_TRY_AGAIN);
        }

        String secretStr = storedSecret.toString();
        boolean result = isOtpValid(secretStr,otp);
        if (!result) {
            throw new OtpVerificationException(MessageConstants.OTP_INVALID);
        }
        if(!user.isTwoFactorEnabled()){
            user.setTwoFactorEnabled(true);
            user.setTwoFactorSecret(secretStr);
            userRepository.save(user);
        }
        redisService.delete(keyTemp2fa);


        return backupCodeService.generateBackupCode(user);

    }

    @Transactional
    @Override
    public void disable2fa(UserEntity user, String password){

        securityService.validatePassword(user, password);

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);
        backupCodeRepository.deleteByUserEntityUserId(user.getUserId());
    }

}
