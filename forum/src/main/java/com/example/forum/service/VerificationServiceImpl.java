package com.example.forum.service;

import com.example.forum.dto.response.ApiResponse;
import com.example.forum.dto.response.VerifyOtpResponse;
import com.example.forum.entity.UserEntity;
import com.example.forum.entity.UserVerificationToken;
import com.example.forum.exception.ResourceNotFoundException;
import com.example.forum.repository.UserRepository;
import com.example.forum.repository.VerificationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final VerificationRepo verificationRepo;
    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private final RedisService redisService;

    private static final String PREFIX = "verification:opt:";
    private static final String PREFIX_ATTEMPT ="attempts:verification:otp:";
    private static final long TIME_ATTEMPTS_EXPIRATION= 900; // 15 phuts
    private static final long EXPIRATION_TIME= 300; // 5 phút
    private static final int MAX_ATTEMPT =5;
    private static final String PREFIX_RESET_TOKEN="password_reset_token:";

    public void sendMail(String email, String token) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(email);
        mail.setSubject("Verification Code");
        mail.setText("Your verification code: "+ token);
        System.out.println("Đã gửi mail đến: " + email);
        javaMailSender.send(mail);
    }

    @Override
    public void sendVerificationEmail(UserEntity userEntity) {

        String token = String.format("%06d", new Random().nextInt(1000000));
        redisService.set(PREFIX+userEntity.getEmail(), token, 300, TimeUnit.SECONDS);
        redisService.set(PREFIX_ATTEMPT+userEntity.getEmail(), 1, TIME_ATTEMPTS_EXPIRATION, TimeUnit.SECONDS);
//        UserVerificationToken userVerificationToken = new UserVerificationToken();
//        userVerificationToken.setToken(token);
//        userVerificationToken.setUser(userEntity);
//        userVerificationToken.setExpiredDate(LocalDateTime.now().plusMinutes(15));
//        userVerificationToken.setSendTime(LocalDateTime.now());
//        userVerificationToken.setSentCount(userVerificationToken.getSentCount()+1);
//        verificationRepo.save(userVerificationToken);

        sendMail(userEntity.getEmail(),token);
    }


    @Override
    public void resendVerificationCode(String email) {

        String attemptKey = PREFIX_ATTEMPT+email;

        Object attemptObj = redisService.get(attemptKey);
        int attempts = (attemptObj == null) ? 0 : Integer.parseInt(attemptObj.toString());

        if(attempts >= MAX_ATTEMPT) {
            throw new IllegalArgumentException("Too many attempts! Please wait 15 minutes.");
        }

        String newToken= String.format("%06d", new Random().nextInt(1000000));

        redisService.set(PREFIX + email, newToken, EXPIRATION_TIME, TimeUnit.SECONDS);
        redisService.increment(attemptKey);

        // xử lí trường hợp khi người dùng bỏ lâu quá, 20p sau mới ấn resent thì lúc đó attempt đã bị xoá
        // và khi redis tìm không thấy sẽ tự tạo ra attempt =1 nhưng không có ttl
        if (attemptObj == null) {
            redisService.setExpire(attemptKey, TIME_ATTEMPTS_EXPIRATION, TimeUnit.SECONDS);
        }

        sendMail(email,newToken);
    }

    @Override
    public VerifyOtpResponse verifyToken (String email, String inputToken) {
//        UserVerificationToken vt= verificationRepo.findByUser_Email(email)
//                .orElseThrow(()->new IllegalArgumentException("verification code not found"));

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found!"));

        String key = PREFIX+email;

        if (!redisService.hasKey(key)) {
            throw new IllegalArgumentException("Code expired or invalid");
        }

        Object verificationToken = redisService.get(key);
        if(!verificationToken.toString().equals(inputToken)) {
            throw new IllegalArgumentException("Wrong code");
        }

        if (!user.getIsVerified()) {
            user.setIsVerified(true);
            userRepository.save(user);
        }

        UUID resetToken = UUID.randomUUID();
        String resetTokenKey= PREFIX_RESET_TOKEN+resetToken;
        redisService.set(resetTokenKey, email, 300, TimeUnit.SECONDS);

        redisService.delete(key);
        redisService.delete(PREFIX_ATTEMPT+email);

        return VerifyOtpResponse.builder()
                .resetToken(resetToken.toString())
                .expiredTime(300)
                .build();
    }
}
