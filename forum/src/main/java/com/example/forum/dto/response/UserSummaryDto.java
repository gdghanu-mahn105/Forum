package com.example.forum.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSummaryDto {
    private Long userId;
    private String username;
    private String email;
    private String avatarUrl;
}