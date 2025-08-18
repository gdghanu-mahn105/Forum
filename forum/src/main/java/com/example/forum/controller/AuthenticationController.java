package com.example.forum.controller;


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
    public ResponseEntity<String> register (
            @Valid @RequestBody RegisterRequest request
    ){
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail (@Valid
            @RequestBody VerifyEmailRequest request
    ) {
        return ResponseEntity.ok(authenticationService.verifyCode(request.getEmail(), request.getCode()));
    }

    @PatchMapping("/resend-verification-code")
    public ResponseEntity<String> resendVerificationCode(
            @Valid @RequestBody ResendEmailRequest request
            ){
        return ResponseEntity.ok(verificationService.resendVerificationCode(request.getEmail()));
    }


    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate (
            @Valid @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/createAdmin")
    public ResponseEntity<AuthenticationResponse> createAdmin (
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(adminService.createAdmin(request));
    }


}
