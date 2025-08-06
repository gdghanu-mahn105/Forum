package com.example.forum.service;

import com.example.forum.entity.UserEntity;

public interface VerificationService {
    void sendVerificationEmail(UserEntity user);

    String verifyToken(String email, String inputToken);

    String resendVerificationCode(String email);
}
