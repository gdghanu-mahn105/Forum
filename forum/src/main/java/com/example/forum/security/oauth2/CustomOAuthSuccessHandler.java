package com.example.forum.security.oauth2;

import com.example.forum.common.constant.MessageConstants;
import com.example.forum.common.utils.RequestUtils;
import com.example.forum.security.jwt.JWTService;
import com.example.forum.entity.UserEntity;
import com.example.forum.repository.UserRepository;
import com.example.forum.service.DeviceService;
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
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomOAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTService jwtService;
    private final UserRepository userRepository;
    private final DeviceService deviceService;

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

            UserEntity user = userRepository.findByProviderAndProviderId(provider, sub).orElseThrow(() -> new RuntimeException(MessageConstants.USER_NOT_FOUND));

            String deviceId = "oauth2-" + provider + "-" + user.getUserId();
            String token = jwtService.generateAccessToken(user,deviceId);
            String rawRefreshToken = UUID.randomUUID().toString();

            String ip= RequestUtils.getClientIp();
            String userAgent = RequestUtils.getUserAgent();

            if (deviceId == null) deviceId = UUID.randomUUID().toString();

            boolean isNewDevice =deviceService.saveUserDevice(user, deviceId, rawRefreshToken, userAgent, ip);
//            if (isNewDevice){
//                emailService.sendAlertNewDeviceLogin(user.getEmail(), userAgent, ip, formatter.format(Instant.now()));
//            }

//            response.setContentType("application/json");
//            response.setCharacterEncoding("UTF-8");
//            response.getWriter().write("{\"token\": \"" + token + "\"}");
            String redirectUrl = "http://localhost:5173/oauth2/success?token=" + token + "&refreshToken=" + rawRefreshToken;
            response.sendRedirect(redirectUrl);
        }
    }

}
