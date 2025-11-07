package com.farmchainx.farmchainx.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.farmchainx.farmchainx.dto.AuthResponse;
import com.farmchainx.farmchainx.dto.LoginRequest;
import com.farmchainx.farmchainx.dto.RegisterRequest;
import com.farmchainx.farmchainx.model.Role;
import com.farmchainx.farmchainx.model.User;
import com.farmchainx.farmchainx.repository.RoleRepository;
import com.farmchainx.farmchainx.repository.UserRepository;
import com.farmchainx.farmchainx.security.JwtUtil;

import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists!");
        }

        if (!Set.of("CONSUMER", "FARMER", "DISTRIBUTER", "RETAILER")
                .contains(request.getRole().toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot register as ADMIN!");
        }

        Role role = roleRepository.findByName("ROLE_" + request.getRole().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(role));
        userRepository.save(user);

        return new AuthResponse(null, role.getName(), request.getEmail());
    }




    public AuthResponse login(LoginRequest login) {

        User user = userRepository.findByEmail(login.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(login.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }

        // ✅ Check if user has admin role
        boolean isAdmin = user.getRoles()
                .stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        // ✅ Primary role: ADMIN if present, else first assigned role
        String primaryRole = isAdmin
                ? "ROLE_ADMIN"
                : user.getRoles()
                    .stream()
                    .map(Role::getName)
                    .findFirst()
                    .orElse("ROLE_CONSUMER");

        // ✅ Generate token
        String token = jwtUtil.generateToken(user.getEmail(), primaryRole, user.getId());

        return new AuthResponse(token, primaryRole, user.getEmail());
    }
}
