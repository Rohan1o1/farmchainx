package com.farmchainx.farmchainx.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
            throw new RuntimeException("Email already exists!");
        }

        Role role = roleRepository.findByName("ROLE_CONSUMER")
                .orElseThrow(() -> new RuntimeException("Consumer role missing"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(role));

        userRepository.save(user);

        return new AuthResponse(null, "ROLE_CONSUMER", request.getEmail());
    }


    public AuthResponse login(LoginRequest login) {

        User user = userRepository.findByEmail(login.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(login.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }

        // ✅ Get first role (Because Set<Role>)
        String role = user.getRoles()
                .stream()
                .map(Role::getName)
                .findFirst()
                .orElse("ROLE_CONSUMER");

        String token = jwtUtil.generateToken(login.getEmail(), role, user.getId());

        return new AuthResponse(token, role, login.getEmail());
    }

}
