package com.example.forum.controller;


import com.example.forum.dto.response.ApiResponse;
import com.example.forum.dto.response.AuthenticationResponse;
import com.example.forum.auth.AuthenticationService;
import com.example.forum.dto.request.AuthenticationRequest;
import com.example.forum.dto.request.RegisterRequest;
import com.example.forum.dto.request.ResendEmailRequest;
import com.example.forum.dto.request.VerifyEmailRequest;
import com.example.forum.service.AdminService;
import com.example.forum.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forum/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final VerificationService verificationService;
    private final AdminService adminService;

    @PostMapping("/register")
    public ResponseEntity<?> register (
            @Valid @RequestBody RegisterRequest request
    ){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "Successfully created, you need to verify your email",
                        authenticationService.register(request)
        ));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail (@Valid
            @RequestBody VerifyEmailRequest request
    ) {
        authenticationService.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(new ApiResponse<>(
               true,
               "successfully, you can login now",
               null
        ));
    }

    @PatchMapping("/resend-verification-code")
    public ResponseEntity<?> resendVerificationCode(
            @Valid @RequestBody ResendEmailRequest request
            ){
        verificationService.resendVerificationCode(request.getEmail());
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Resent verification code!",
                null
        ));
    }


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser (
            @Valid @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok( new ApiResponse<>(
                true,
                "Logging in successfully",
                authenticationService.authenticate(request)

        ));
    }

    @PostMapping("/createAdmin")
    public ResponseEntity<?> createAdmin (
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Successfully create ADMIN",
                        adminService.createAdmin(request)
                )
        );
    }


}
