package com.example.forum.controller;


import com.example.forum.auth.AuthenticationResponse;
import com.example.forum.auth.AuthenticationService;
import com.example.forum.dto.AuthenticationRequest;
import com.example.forum.dto.RegisterRequest;
import com.example.forum.dto.ResendEmailRequest;
import com.example.forum.dto.VerifyEmailRequest;
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
}
