package com.wepong.pongdang.config;

import com.wepong.pongdang.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/register", "/api/auth/login", "/ws/**", "/api/quizzes/**").permitAll()
            .requestMatchers("/api/auth/phone/**").permitAll()
                            .requestMatchers("/api/game/**").permitAll()
            .requestMatchers("/api/email/**").permitAll()
            .requestMatchers("/api/**").permitAll() // API 테스트용
            .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .cors(cors -> cors.disable());

        return http.build();
    }
}
