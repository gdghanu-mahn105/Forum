package com.example.forum.dto.request;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}
