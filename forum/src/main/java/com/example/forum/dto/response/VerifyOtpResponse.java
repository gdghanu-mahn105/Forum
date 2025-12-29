package com.example.forum.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyOtpResponse {
    private String resetToken;
    private long expiredTime;
}
