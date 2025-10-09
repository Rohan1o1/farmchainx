package com.farmchainx.farmchainx.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    // This bean is used to encrypt passwords before saving them
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for API testing
            .authorizeHttpRequests(authz -> authz
                // Public access for registration and login
                .requestMatchers("/api/auth/**").permitAll()
                // All other requests must be authenticated
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> httpBasic.disable()) // Disable basic auth
            .formLogin(form -> form.disable()); // Disable form login
        return http.build();
    }
}
