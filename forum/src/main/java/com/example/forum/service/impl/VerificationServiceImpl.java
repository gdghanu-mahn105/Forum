package com.example.forum.service.impl;

import com.example.forum.constant.AppConstants;
import com.example.forum.constant.MessageConstants;
import com.example.forum.dto.response.VerifyOtpResponse;
import com.example.forum.entity.UserEntity;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.repository.UserRepository;
import com.example.forum.service.CacheService;
import com.example.forum.service.EmailService;
import com.example.forum.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final CacheService redisService;

    @Value("${app.otp.verification-attempt.window-seconds}")
    private long attemptVerificationWindow; // 15 phuts

    @Value("${app.otp.expiration-seconds}")
    private long otpExpirationTime; // 5 phút

    @Value("${app.otp.max-attempts}")
    private int verificationMaxAttempts;

    @Value("${app.redis.verification.temp-token}")
    private int tempTokenExpirationTime;

    @Override
    public void sendVerificationEmail(UserEntity userEntity) {

        String token = String.format("%06d", new Random().nextInt(AppConstants.OTP_GENERATION_BOUND));
        redisService.set(AppConstants.PREFIX_VERIFICATION_OTP +userEntity.getEmail(), token, otpExpirationTime, TimeUnit.SECONDS);
        redisService.set(AppConstants.PREFIX_VERIFICATION_ATTEMPT+userEntity.getEmail(), AppConstants.INITIAL_ATTEMPT_VALUE, attemptVerificationWindow, TimeUnit.SECONDS);

        emailService.sendOtpMail(userEntity.getEmail(), token);
    }


    @Override
    public void resendVerificationCode(String email) {

        String attemptKey = AppConstants.PREFIX_VERIFICATION_ATTEMPT+email;

        Object attemptObj = redisService.get(attemptKey);
        int attempts = (attemptObj == null) ? 0 : Integer.parseInt(attemptObj.toString());

        if(attempts >= verificationMaxAttempts) {
            throw new IllegalArgumentException(MessageConstants.OTP_LIMIT_REACHED);
        }

        String newToken= String.format("%06d", new Random().nextInt(AppConstants.OTP_GENERATION_BOUND));

        redisService.set(AppConstants.PREFIX_VERIFICATION_OTP + email, newToken, otpExpirationTime, TimeUnit.SECONDS);
        redisService.increment(attemptKey);

        // xử lí trường hợp khi người dùng bỏ lâu quá, 20p sau mới ấn resent thì lúc đó attempt đã bị xoá
        // và khi redis tìm không thấy sẽ tự tạo ra attempt =1 nhưng không có ttl
        if (attemptObj == null) {
            redisService.setExpire(attemptKey, attemptVerificationWindow, TimeUnit.SECONDS);
        }

        emailService.sendOtpMail(email, newToken);
    }

    @Override
    public VerifyOtpResponse verifyToken (String email, String inputToken) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        String key = AppConstants.PREFIX_VERIFICATION_OTP+email;

        if (!redisService.hasKey(key)) {
            throw new IllegalArgumentException(MessageConstants.OTP_INVALID);
        }

        Object verificationToken = redisService.get(key);
        if(!verificationToken.toString().equals(inputToken)) {
            throw new IllegalArgumentException(MessageConstants.WRONG_OTP_CODE);
        }

        if (!user.getIsVerified()) {
            user.setIsVerified(true);
            userRepository.save(user);
        }

        UUID resetToken = UUID.randomUUID();
        String resetTokenKey= AppConstants.PREFIX_RESET_TOKEN+resetToken;
        redisService.set(resetTokenKey, email, tempTokenExpirationTime, TimeUnit.SECONDS);

        redisService.delete(key);
        redisService.delete(AppConstants.PREFIX_VERIFICATION_ATTEMPT+email);

        return VerifyOtpResponse.builder()
                .resetToken(resetToken.toString())
                .expiredTime(tempTokenExpirationTime)
                .build();
    }
}
