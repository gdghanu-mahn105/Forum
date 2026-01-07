package com.example.forum.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String accessToken;

    private String refreshToken;

    private String deviceId;

    private UserSummaryDto user;

    private boolean requiresTwoFactor;

    private String message;
}
