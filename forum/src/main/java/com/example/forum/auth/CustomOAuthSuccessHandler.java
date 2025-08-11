package com.example.forum.auth;

import com.example.forum.config.JWTService;
import com.example.forum.entity.UserEntity;
import com.example.forum.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomOAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTService jwtService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        if(authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
            String provider = authToken.getAuthorizedClientRegistrationId();

            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String sub = oAuth2User.getName();
            System.out.println("OAuth2User name(): " + name);
            System.out.println("OAuth2User email: " +email);
            System.out.println(oAuth2User.getAttributes());

            UserEntity user = userRepository.findByProviderAndProviderId(provider, sub).orElseThrow(() -> new RuntimeException("User not found"));

            String token = jwtService.generateToken(user);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"token\": \"" + token + "\"}");

        }
    }

}
