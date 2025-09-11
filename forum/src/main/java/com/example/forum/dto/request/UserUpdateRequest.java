package com.example.forum.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateRequest {
    private String username;
    private String avatarUrl;
}
