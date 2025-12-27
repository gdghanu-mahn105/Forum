package com.example.forum.security;


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
import java.util.List;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserDetailsService userDetailsService;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-resources",
            "/webjars",
            "/forum/auth"
//            "/forum/home",
//            "/login/oauth2",
//            "/forum/posts",
//            "/forum/tags"
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

        // === BƯỚC 1: KIỂM TRA XEM REQUEST CÓ PHẢI LÀ PUBLIC KHÔNG ===
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
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Invalid authorization header");
            return;
        }

        jwtToken = authHeader.substring(7);
        userEmail= jwtService.extractUsername(jwtToken);

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
}
