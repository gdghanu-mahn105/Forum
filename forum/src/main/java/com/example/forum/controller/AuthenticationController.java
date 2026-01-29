package com.example.forum.controller;


import com.example.forum.dto.request.*;
import com.example.forum.dto.response.ApiResponse;
import com.example.forum.service.impl.AuthenticationServiceImpl;
import com.example.forum.dto.response.UserSummaryDto;
import com.example.forum.entity.UserEntity;
import com.example.forum.service.impl.AdminServiceImpl;
import com.example.forum.service.VerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/forum/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationServiceImpl authenticationService;
    private final VerificationService verificationService;
    private final AdminServiceImpl adminService;

    // API mới: Lấy thông tin user hiện tại dựa trên Token
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        // Spring Security sẽ tự động lấy user từ JWT Token
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Not authenticated", null));
        }

        UserEntity user = (UserEntity) authentication.getPrincipal();

        // Trả về UserSummaryDto (bạn đã có hàm mapper này)
        UserSummaryDto userDto = new UserSummaryDto(
                user.getUserId(),
                user.displayUsername(),
                user.getEmail(),
                user.getAvatarUrl()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "User fetched", userDto));
    }

    @PostMapping(value = "/register")
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
        return ResponseEntity.ok(new ApiResponse<>(
               true,
               "successfully, you can login now",
                authenticationService.verifyCode(request.getEmail(), request.getCode())
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
        adminService.createAdmin(request);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Successfully create ADMIN",
                        null
                )
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshtoken(
            @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Refreshed token",
                        authenticationService.refreshToken(request.getRefreshToken())
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestBody LogoutRequest request,
            HttpServletRequest httpServletRequest
    ){
        String authHeader = httpServletRequest.getHeader("Authorization");
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")){
            accessToken = authHeader.substring(7);
        }
        authenticationService.logout(request, accessToken);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Logout successfully",
                        null
                )
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword( @RequestBody ForgotPasswordRequest request){
        authenticationService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Your verification code has been send to your email",
                        null
                )
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request){
        authenticationService.resetPassword(request);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Reset Password Successfully",
                        null
                )
        );
    }

    @PostMapping("/2fa-login")
    public ResponseEntity<?> verify2faLogin (
            @Valid @RequestBody Verify2faLoginRequest request
    ) {
        return ResponseEntity.ok( new ApiResponse<>(
                true,
                "Logging in successfully",
                authenticationService.verifyTwoFactorLogin(request.getEmail(),request.getOtpCode(), request.getDeviceId())
        ));
    }
}
