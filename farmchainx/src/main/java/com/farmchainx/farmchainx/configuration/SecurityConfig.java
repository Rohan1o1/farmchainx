package com.farmchainx.farmchainx.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.farmchainx.farmchainx.jwt.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ✅ feedback route MUST come before /api/products/**
                .requestMatchers("/api/products/*/feedback").permitAll()

                // ✅ Public routes
                .requestMatchers(
                        "/api/auth/**",
                        "/uploads/**",
                        "/api/verify/**",
                        "/api/products/*/qrcode/download"
                ).permitAll()

                // ✅ Product routes need farmer / supply chain roles
                .requestMatchers("/api/products/**")
                        .hasAnyRole("FARMER", "DISTRIBUTER", "RETAILER", "ADMIN")

                // ✅ Tracking routes
                .requestMatchers("/api/track/**")
                        .hasAnyRole("DISTRIBUTER", "RETAILER", "ADMIN")

                // ✅ Admin routes
                .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                // ✅ Everything else needs authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
