package com.example.forum.service;

import com.example.forum.dto.response.ApiResponse;
import com.example.forum.entity.UserEntity;
import com.example.forum.entity.UserVerificationToken;
import com.example.forum.repository.UserRepository;
import com.example.forum.repository.VerificationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final VerificationRepo verificationRepo;
    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;


    public void sendMail(String email, String token) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(email);
        mail.setSubject("Verification Code");
        mail.setText("Your verification code: "+ token);
        javaMailSender.send(mail);
    }

    @Override
    public void sendVerificationEmail(UserEntity userEntity) {

        String token = String.format("%06d", new Random().nextInt(1000000));
        UserVerificationToken userVerificationToken = new UserVerificationToken();
        userVerificationToken.setToken(token);
        userVerificationToken.setUser(userEntity);
        userVerificationToken.setExpiredDate(LocalDateTime.now().plusMinutes(15));
        userVerificationToken.setSendTime(LocalDateTime.now());
        userVerificationToken.setSentCount(userVerificationToken.getSentCount()+1);
        verificationRepo.save(userVerificationToken);

        sendMail(userEntity.getEmail(),token);
    }


    @Override
    public ApiResponse<?> resendVerificationCode(String email) {

        String newToken= String.format("%06d", new Random().nextInt(1000000));

        UserVerificationToken user = verificationRepo.findByUser_Email(email)
                .orElseThrow(()-> new IllegalArgumentException("User not found"));

        if(user.getSentCount()>=5){
            throw new IllegalArgumentException("too many send action!");
        }
        if(!user.getSendTime().plusMinutes(1).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("you can resent after 60s");
        }
        user.setToken(newToken);
        user.setExpiredDate(LocalDateTime.now().plusMinutes(15));
        user.setSendTime(LocalDateTime.now());
        user.setSentCount(user.getSentCount()+1);
        verificationRepo.save(user);

        sendMail(email,newToken);

        return ApiResponse.builder()
                .success(true)
                .message("Resent!")
                .build();

    }

    @Override
    public void verifyToken (String email, String inputToken) {
        UserVerificationToken vt= verificationRepo.findByUser_Email(email)
                .orElseThrow(()->new IllegalArgumentException("verification code not found"));

        if(!vt.getToken().equals(inputToken)) {
            throw new IllegalArgumentException("Wrong code");
        }

        if(vt.getExpiredDate().isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("Code expired");
        }

        UserEntity user = vt.getUser();
        user.setIsVerified(true);
        userRepository.save(user);
        verificationRepo.delete(vt);
    }
}
