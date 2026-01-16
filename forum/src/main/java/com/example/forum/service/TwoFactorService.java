package com.example.forum.service;

import com.example.forum.dto.response.TwoFactorResponse;
import com.example.forum.entity.UserEntity;

import java.util.List;

public interface TwoFactorService {
    String generateNewSecret();

    String generateQrCodeUri(String secret, String email);

    boolean isOtpValid(String secret, int code) ;

    TwoFactorResponse enableTwoFactor(String email);

    List<String> verifyOtp(String email, int otp);

    void disable2fa(UserEntity user, String password);

    boolean is2faEnable();
}
