package com.example.forum.core.config;

import com.example.forum.security.oauth2.CustomOidc2UserService;
import com.example.forum.security.oauth2.CustomOAuthSuccessHandler;
import com.example.forum.security.jwt.JWTAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JWTAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOidc2UserService customOidc2UserService;
    private final CustomOAuthSuccessHandler customOAuthSuccessHandler;

    private static final String[] SWAGGER_PATHS = {
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/webjars/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER_PATHS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/forum/auth/**"
                                , "/forum/home/**"
                                ,"/login/oauth2/**",
                                "/oauth2/**",
                                "/forum/upload/avatar")
                        .permitAll()
                        .requestMatchers("/forum/admin/**").hasRole("ADMIN")
                        .requestMatchers("/forum/user/**", "/forum/auth/me").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Bỏ qua OAuth endpoints
                            String path = request.getRequestURI();
                            if (path.startsWith("/oauth2") || path.contains("/oauth2/")) {
                                // Let OAuth flow handle it
                                response.sendRedirect("/oauth2/authorization/google");
                            } else {
                                response.setContentType("application/json");
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized\"}");
                            }
                        })
                )

                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(auth -> auth
                                .baseUri("/oauth2/authorization")  // ✅ Đảm bảo match với frontend
                        )
                        .userInfoEndpoint(user ->
                                user.oidcUserService(customOidc2UserService)
                        )
                        .successHandler(customOAuthSuccessHandler)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .logout(logout -> logout.disable())
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. Cho phép Frontend của bạn (đổi port nếu cần)
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        // 2. Cho phép các method phổ biến
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 3. Cho phép tất cả các header (Authorization, Content-Type...)
        configuration.setAllowedHeaders(List.of("*"));

        // 4. Cho phép gửi kèm cookie/credentials (quan trọng cho login)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
