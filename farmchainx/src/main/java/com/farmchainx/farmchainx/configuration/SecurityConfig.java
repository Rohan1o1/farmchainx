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

            // ✅ Allow controller exceptions to reach client instead of being converted to 403
            .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) -> {}))

            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                    // ✅ MUST BE FIRST to avoid 403 on register/login
                    .requestMatchers("/api/auth/**").permitAll()

                    // ✅ Allow Spring Boot default error page (prevents AuthorizationDeniedException logs)
                    .requestMatchers("/error").permitAll()

                    .requestMatchers("/api/products/*/feedback").permitAll()
                    .requestMatchers(
                            "/uploads/**",
                            "/api/verify/**",
                            "/api/products/*/qrcode/download"
                    ).permitAll()

                    // ✅ Public product viewing
                    .requestMatchers("/api/products", "/api/products/*").permitAll()

                    // ✅ Product modification for roles
                    .requestMatchers("/api/products/**")
                            .hasAnyRole("FARMER", "DISTRIBUTER", "RETAILER", "ADMIN")

                    // ✅ Tracking
                    .requestMatchers("/api/track/**")
                            .hasAnyRole("DISTRIBUTER", "RETAILER", "ADMIN")
                            .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                    // ✅ Admin only
                    .requestMatchers("/api/admin/**")
                            .hasRole("ADMIN")
                            
                     

                    // ✅ Everything else requires authentication
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
