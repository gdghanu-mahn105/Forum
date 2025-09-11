package com.example.forum.security;

import com.example.forum.entity.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    public UserEntity getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (UserEntity) auth.getPrincipal();
    }

    public Long getCurrentUserId() {
        UserEntity user = getCurrentUser();
        return (user != null) ? user.getUserId() : null;
    }
}
