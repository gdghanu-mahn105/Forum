package com.example.forum.dto.response;


import com.example.forum.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private Long userId;
    private String email;
    private String userName;
    private String avatarUrl;
    private Set<Role> roles;
    private Boolean isVerified;
    private LocalDateTime createdAt;


}
