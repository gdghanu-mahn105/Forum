package com.example.forum.security;

import com.example.forum.entity.UserEntity;
import com.example.forum.exception.NotLoggedInException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityService {
    private final PasswordEncoder passwordEncoder;

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

    public void validatePassword(UserEntity user, String rawPassword) {
        if (rawPassword == null || !passwordEncoder.matches(rawPassword, user.getUserPassword())) {
            throw new BadCredentialsException("Mật khẩu xác nhận không chính xác");
        }
    }
}