package com.example.forum.service;

public interface EmailService {
    void sendSimpleMessageMail(String to, String subject, String text);

    void sendOtpMail(String toMail, String otpCode);

    void sendAlertNewDeviceLogin(String toEmail, String userAgent,String ipAddress, String loginTime);
}
