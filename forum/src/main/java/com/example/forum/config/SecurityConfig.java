package com.example.forum.config;

import com.example.forum.auth.CustomOidc2UserService;
import com.example.forum.auth.CustomOAuthSuccessHandler;
import com.example.forum.security.JWTAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

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
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER_PATHS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/forum/auth/**"
                                , "/forum/home/**"
                                ,"/login/oauth2/**")
                        .permitAll()
                        .requestMatchers("/forum/admin/**").hasRole("ADMIN")
                        .requestMatchers("/forum/user/**", "/forum/auth/me").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )

//                .oauth2Login(oauth -> oauth
//                        .userInfoEndpoint(user ->
//                                user.oidcUserService(customOidc2UserService)
//                        )
//                        .successHandler(customOAuthSuccessHandler)
//                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/forum/home").permitAll()
                )
                .authenticationProvider(authenticationProvider)
//                .addFilterBefore(corsFilter(), JWTAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // === THÊM BEAN NÀY VÀO ===
    /**
     * Bean này dùng để ngăn Spring Boot tự động đăng ký
     * JWTAuthenticationFilter vào chuỗi filter chung.
     * Chúng ta muốn Spring Security (bên trên) toàn quyền kiểm soát nó.
     */
//    @Bean
//    public FilterRegistrationBean<JWTAuthenticationFilter> jwtAuthenticationFilterRegistration(JWTAuthenticationFilter filter) {
//        FilterRegistrationBean<JWTAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
//        registration.setEnabled(false); // <-- DÒNG NÀY SẼ SỬA LỖI
//        return registration;
//    }

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowCredentials(true);
//        config.setAllowedOrigins(List.of("http://localhost:3000"));
//        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        config.setAllowedHeaders(List.of("*"));
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        // Áp dụng cấu hình CORS này cho TẤT CẢ các đường dẫn
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }

}
