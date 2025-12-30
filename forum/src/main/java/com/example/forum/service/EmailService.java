package com.example.forum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;


    @Async
    public void sendMail(String to, String subject, String text) {
        try {

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(text);

            javaMailSender.send(mail);
            System.out.println("Đã gửi mail đến: "+ to);

        } catch (Exception e) {
            System.out.printf("Lỗi khi gửi mail: ", e.getMessage());
        }
    }
}
