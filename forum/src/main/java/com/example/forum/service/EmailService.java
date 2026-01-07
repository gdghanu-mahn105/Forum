package com.example.forum.service;

public interface EmailService {
    void sendMail(String to, String subject, String text);
}
