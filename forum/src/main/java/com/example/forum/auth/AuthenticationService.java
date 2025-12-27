package com.example.forum.auth;

import com.example.forum.dto.response.ApiResponse;
import com.example.forum.dto.response.AuthenticationResponse;
import com.example.forum.dto.response.UserSummaryDto;
import com.example.forum.exception.EmailAlreadyExistsException;
import com.example.forum.security.JWTService;
import com.example.forum.dto.request.AuthenticationRequest;
import com.example.forum.dto.request.RegisterRequest;
import com.example.forum.entity.Role;
import com.example.forum.entity.UserEntity;
import com.example.forum.repository.RoleRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final AuthenticationManager authManager;
    private final VerificationService verificationService;
    private final RoleRepository roleRepository;

    public UserSummaryDto register(RegisterRequest request) {

        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException( "Email was used");
        }

        if(request.getPassword()==null){
            throw new IllegalArgumentException("Password must be filled");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(()-> new RuntimeException("role not found"));


        var user= UserEntity.builder()
                .userName(request.getUserName())
                .email(request.getEmail())
                .userPassword(passwordEncoder.encode(request.getPassword()))
                .avatarUrl("https://cdn-icons-png.flaticon.com/512/9815/9815472.png") // default avatar url
                .roles(Set.of(userRole))
                .isVerified(false)
                .build();
        userRepository.save(user);

        verificationService.sendVerificationEmail(user);

        return new UserSummaryDto(user.getUserId(),
                user.displayUsername(),
                user.getEmail(),
                user.getAvatarUrl());
    }

    public void verifyCode(String email, String code) {
        verificationService.verifyToken(email, code);
    }



    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        var user = userRepository.findByEmail(request.getEmail())
                 .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            var jwtToken = jwtService.generateToken(user);
            AuthenticationResponse response=  AuthenticationResponse.builder()
                    .Token(jwtToken)
                    .user(UserSummaryDto.builder()
                            .userId(user.getUserId())
                            .username(user.displayUsername())
                            .email(user.getEmail())
                            .avatarUrl(user.getAvatarUrl())
                            .build())
                    .build();
            return response;
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password!");
        } catch (DisabledException ex){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Your account is not verified");
        } catch (LockedException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is locked");
        }

    }
}
