package com.example.forum.dto.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class UserSummaryDto {
    private Long userId;
    private String username;
    private String email;
    private String avatarUrl;
}