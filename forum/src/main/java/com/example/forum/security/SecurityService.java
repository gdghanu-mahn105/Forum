package com.example.forum.security;

import com.example.forum.entity.UserEntity;
import com.example.forum.exception.NotLoggedInException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    public UserEntity getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new NotLoggedInException("You need to login to use this feature");
        }
        return (UserEntity) auth.getPrincipal();
    }

    public Long getCurrentUserId() {
        UserEntity user = getCurrentUser();
        return (user != null) ? user.getUserId() : null;
    }


    public UserEntity getCurrentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        // Giả sử 'getPrincipal()' trả về UserEntity (hoặc UserDetails)
        return (UserEntity) authentication.getPrincipal();
    }
}