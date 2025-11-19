package com.farmchainx.farmchainx.configuration;

import jakarta.servlet.http.HttpServletResponse;
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
@EnableMethodSecurity(prePostEnabled = true) // Enables @PreAuthorize
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

            // Custom JSON error responses for better frontend handling
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, authEx) -> {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"error\": \"Unauthorized - Please log in\"}");
                })
                .accessDeniedHandler((req, res, accessEx) -> {
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"error\": \"Forbidden - Insufficient permissions\"}");
                })
            )

            .authorizeHttpRequests(auth -> auth
                // Public endpoints - NO AUTH REQUIRED
                .requestMatchers("/api/auth/**").permitAll()                              // login, register, refresh
                .requestMatchers("/error", "/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                // Public static files
                .requestMatchers("/uploads/**").permitAll()

                // QR CODE & PUBLIC VERIFICATION - ANYONE CAN SCAN
                .requestMatchers("/api/verify/**").permitAll()
                .requestMatchers("/api/products/*/qrcode/download").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/by-uuid/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/{id}/public").permitAll()

                // Public product listing (for marketplace or search if any)
                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()

                // Role-based access
                .requestMatchers("/api/products/upload", "/api/products/**")
                .hasAnyRole("FARMER", "DISTRIBUTOR", "DISTRIBUTER", "RETAILER", "ADMIN")

            .requestMatchers("/api/track/**")
                .hasAnyRole("DISTRIBUTOR", "DISTRIBUTER", "RETAILER", "ADMIN", "FARMER")// Farmers can also add notes if needed

                .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")

                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // JWT Filter runs before Spring Security's default filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}