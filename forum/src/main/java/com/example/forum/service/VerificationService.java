package com.example.forum.service;

import com.example.forum.dto.response.ApiResponse;
import com.example.forum.entity.UserEntity;

public interface VerificationService {
    void sendVerificationEmail(UserEntity user);

    void verifyToken(String email, String inputToken);

    void resendVerificationCode(String email);
}
