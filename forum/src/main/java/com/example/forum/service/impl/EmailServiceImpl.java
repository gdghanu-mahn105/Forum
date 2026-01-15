package com.example.forum.service.impl;

import com.example.forum.common.constant.MessageConstants;
import com.example.forum.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine template;


    @Async
    @Override
    public void sendSimpleMessageMail(String to, String subject, String text) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(text);

            javaMailSender.send(mail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    @Override
    public void sendOtpMail(String toMail, String otpCode){

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");

            Context context = new Context();
            context.setVariable("otpCode", otpCode);
            String htmlContent = template.process("email-otp", context);

            helper.setTo(toMail);
            helper.setSubject(MessageConstants.SUBJECT_OTP_MAIL);
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e){
            e.printStackTrace();
        }
    }

    @Async
    @Override
    public void sendAlertNewDeviceLogin(String toEmail, String userAgent,String ipAddress, String loginTime){

        try{
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");

            Context context = new Context();
            context.setVariable("deviceName", userAgent);
            context.setVariable("ipAddress", ipAddress);
            context.setVariable("loginTime", loginTime);

            String htmlContent = template.process("login-alert", context);
            helper.setTo(toEmail);
            helper.setSubject(MessageConstants.SUBJECT_NEW_DEVICE_LOGIN);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);

        } catch (MessagingException e){
            e.printStackTrace();
        }
    }
}
