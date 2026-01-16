package com.example.forum.controller;

import com.example.forum.service.impl.TwoFactorServiceImpl;
import com.example.forum.dto.request.OtpInputRequest;
import com.example.forum.dto.request.PasswordConfirmRequest;
import com.example.forum.dto.response.ApiResponse;
import com.example.forum.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forum/user/2af")
@RequiredArgsConstructor
public class TwoFactorController {
    private final TwoFactorServiceImpl twoFactorService;

    @GetMapping("/setup")
    public ResponseEntity<?> setup2af(
            @AuthenticationPrincipal UserEntity user
            ){
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "get secret key successfully",
                        twoFactorService.enableTwoFactor(user.getEmail())
                )
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<?> Verify2af(
            @RequestBody OtpInputRequest request,
            @AuthenticationPrincipal UserEntity user
            ){
        return ResponseEntity.ok(
                        new ApiResponse<>(
                                true,
                                "Your account is verified, Your backup code here!",
                                twoFactorService.verifyOtp(user.getEmail(), request.getOtpCode())
                        )
                );
    }

    @PostMapping("/disable-2fa")
    public ResponseEntity<?> disable2fa(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody PasswordConfirmRequest request
    ) {

        twoFactorService.disable2fa(user,request.getPassword());

        return ResponseEntity.ok(
                        new ApiResponse<>(
                                true,
                                "2FA disabled successfully",
                                null));
    }

    @GetMapping("/isEnable")
    public ResponseEntity<?> is2faEnable(){
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Get 2fa status",
                        twoFactorService.is2faEnable()
                )
        );
    }
}
