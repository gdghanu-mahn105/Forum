package com.example.forum.service;

import com.example.forum.auth.AuthenticationResponse;
import com.example.forum.config.JWTService;
import com.example.forum.dto.RegisterRequest;
import com.example.forum.entity.Role;
import com.example.forum.entity.UserEntity;
import com.example.forum.repository.RoleRepository;
import com.example.forum.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    public AuthenticationResponse createAdmin(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalArgumentException("Role ADMIN not found"));

        UserEntity admin = new UserEntity();
        admin.setUserName(request.getUserName());
        admin.setEmail(request.getEmail());
        admin.setUserPassword(passwordEncoder.encode(request.getPassword()));
        admin.setRoles(Set.of(adminRole));
        admin.setIsVerified(true);

        userRepository.save(admin);

        String jwt = jwtService.generateToken(admin);

        return AuthenticationResponse.builder()
                .Token(jwt)
                .build();
    }
}

