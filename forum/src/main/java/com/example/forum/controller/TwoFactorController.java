package com.example.forum.controller;

import com.example.forum.auth.TwoFactorService;
import com.example.forum.dto.request.OtpInputRequest;
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
    private final TwoFactorService twoFactorService;

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
        twoFactorService.verifyOtp(user.getEmail(), request.getOtpCode());
        return ResponseEntity.ok(
                        new ApiResponse<>(
                                true,
                                "Your account is verified",
                                null
                        )
                );
    }
}
