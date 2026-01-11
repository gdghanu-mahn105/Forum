package com.example.forum.security.jwt;

import com.example.forum.constant.AppConstants;
import com.example.forum.service.impl.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserDetailsService userDetailsService;
    private final RedisService redisService;
    private static final List<String> PUBLIC_PATHS = List.of(
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-resources",
            "/webjars"
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String userEmail;

        final String requestURI = request.getRequestURI();

        boolean isPublicPath = PUBLIC_PATHS.stream().anyMatch(requestURI::startsWith);

        if (isPublicPath) {
            filterChain.doFilter(request, response); // Cho request đi tiếp
            return; // Dừng lại, không kiểm tra JWT
        }

        if(request.getServletPath().startsWith("/forum/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request,response);
            return;
        }


        jwtToken = authHeader.substring(7);

        if (redisService.hasKey(AppConstants.BLACKLIST_KEY +jwtToken)){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token has been revoked (Logout)");
            return;
        }

        userEmail= jwtService.extractUsername(jwtToken);
        Long userId= jwtService.extractUserId(jwtToken);
        Date iatDate= jwtService.extractIssuedDate(jwtToken);
        String deviceId = jwtService.extractDeviceId(jwtToken);

        if(deviceId!= null && isDeviceRevoked(deviceId, iatDate, userId)){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Device session revoked");
            return;
        }


        if(userEmail !=null && SecurityContextHolder.getContext().getAuthentication()==null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            if(jwtService.isValidToken(jwtToken,userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request,response);

    }

    private boolean isDeviceRevoked(String deviceId, Date tokenIssuedAt, Long userId){

        String userKey = AppConstants.REVOKED_USER_KEY + userId;
        Object userRevokedAt = redisService.get(userKey);
        if (userRevokedAt != null) {
            if (tokenIssuedAt.getTime() < Long.parseLong(userRevokedAt.toString())) {
                return true; // nếu token được tạo ra trước thời điẻm -> chặn
            }
        }


        String redisKey = AppConstants.REVOKED_DEVICE_KEY + deviceId;
        Object revokedAtValue = redisService.get(redisKey);

        if (revokedAtValue != null) {
            long revokedAt = Long.parseLong(revokedAtValue.toString());
            return tokenIssuedAt.getTime() < revokedAt;
        }
        return false;
    }
}
