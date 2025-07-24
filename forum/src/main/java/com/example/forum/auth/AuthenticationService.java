package com.example.forum.auth;

import com.example.forum.config.JWTService;
import com.example.forum.dto.AuthenticationRequest;
import com.example.forum.dto.RegisterRequest;
import com.example.forum.entity.UserEntity;
import com.example.forum.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final AuthenticationManager authManager;

    public AuthenticationResponse register(RegisterRequest request) {

        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email was used");
        }

        var user= UserEntity.builder()
                .userName(request.getUserName())
                .email(request.getEmail())
                .userPassword(passwordEncoder.encode(request.getPassword()))
                .roleType(UserEntity.roleType.USER)
                .build();
        userRepository.save(user);
        var JWTtoken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .Token(JWTtoken)
                .build();

    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user= userRepository.findByEmail(request.getEmail()).orElseThrow();

        var JWToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .Token(JWToken)
                .build();
    }
}
