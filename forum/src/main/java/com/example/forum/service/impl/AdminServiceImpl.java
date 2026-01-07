package com.example.forum.service.impl;

import com.example.forum.dto.response.UserSummaryDto;
import com.example.forum.dto.request.RegisterRequest;
import com.example.forum.entity.Role;
import com.example.forum.entity.UserEntity;
import com.example.forum.repository.RoleRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserSummaryDto createAdmin(RegisterRequest request) {

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

        UserEntity savedAdmin= userRepository.save(admin);

        return UserSummaryDto.builder()
                .userId(savedAdmin.getUserId())
                .username(savedAdmin.displayUsername())
                .email(savedAdmin.getEmail())
                .avatarUrl(savedAdmin.getAvatarUrl())
                .build();
    }
}

