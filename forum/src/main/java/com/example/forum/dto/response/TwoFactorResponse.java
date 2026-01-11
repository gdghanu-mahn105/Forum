package com.example.forum.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TwoFactorResponse {
    private String secret;
    private String qrUrl;
}
