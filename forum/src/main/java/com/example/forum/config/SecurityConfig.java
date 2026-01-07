package com.example.forum.config;

import com.example.forum.security.oauth2.CustomOidc2UserService;
import com.example.forum.security.oauth2.CustomOAuthSuccessHandler;
import com.example.forum.security.jwt.JWTAuthenticationFilter;
import lombok.RequiredArgsConstructor;
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
                .logout(logout -> logout.disable())
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
