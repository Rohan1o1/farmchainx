package com.farmchainx.farmchainx.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth

                // ✅ Public routes
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/verify/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/api/products/*/qrcode/download").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                // 🧑‍🌾 Farmers — upload + generate QR
                .requestMatchers(HttpMethod.POST, "/api/products/upload").hasRole("FARMER")
                .requestMatchers(HttpMethod.POST, "/api/products/*/qrcode").hasAnyRole("FARMER","ADMIN")

                // 🔗 Supply chain update
                .requestMatchers("/api/track/update").hasAnyRole("DISTRIBUTOR","RETAILER","ADMIN")

                // 🧑‍💼 Admin
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // All others → must be logged in
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
