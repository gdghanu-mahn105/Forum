package com.example.forum.auth;

import com.example.forum.config.JWTService;
import com.example.forum.dto.AuthenticationRequest;
import com.example.forum.dto.RegisterRequest;
import com.example.forum.entity.UserEntity;
import com.example.forum.repository.UserRepository;
import com.example.forum.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final AuthenticationManager authManager;
    private final VerificationService verificationService;

    public String register(RegisterRequest request) {

        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email was used");
        }

        if(request.getPassword()==null){
            throw new IllegalArgumentException("Password must be filled");
        }

        var user= UserEntity.builder()
                .userName(request.getUserName())
                .email(request.getEmail())
                .userPassword(passwordEncoder.encode(request.getPassword()))
                .roleType(UserEntity.roleType.USER)
                .isVerified(false)
                .build();
        userRepository.save(user);

        verificationService.sendVerificationEmail(user);

//        var JWTtoken = jwtService.generateToken(user);
//        return AuthenticationResponse.builder()
//                .Token(JWTtoken)
//                .build();
        return "Registration successful! Please check your email for the verification code.";
    }

    public String verifyCode(String email, String code) {
        return verificationService.verifyToken(email, code); // gọi lại service đã viết
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

            var JWToken = jwtService.generateToken(user);
            return AuthenticationResponse.builder()
                    .Token(JWToken)
                    .build();

        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password!");
        } catch (DisabledException ex){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Your account is not verified");
        } catch (LockedException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is locked");
        }

    }
}
