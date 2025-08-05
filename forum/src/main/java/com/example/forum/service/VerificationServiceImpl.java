package com.example.forum.service;

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

    @Override
    public void sendVerificationEmail(UserEntity userEntity) {
        String token = String.format("%06d", new Random().nextInt(1000000));
        UserVerificationToken userVerificationToken = new UserVerificationToken();
        userVerificationToken.setToken(token);
        userVerificationToken.setUser(userEntity);
        userVerificationToken.setExpiredDate(LocalDateTime.now().plusMinutes(15));
        verificationRepo.save(userVerificationToken);

        System.out.println("Sending email from: " + javaMailSender);
        System.out.println("Sending email to: " + userEntity.getEmail());

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(userEntity.getEmail());
        mail.setSubject("Verification code");
        mail.setText("Your verification code: "+ token);
        javaMailSender.send(mail);
    }

    @Override
    public String verifyToken (String email, String inputToken) {
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

        return"Your account is successfully activated";

    }
}
