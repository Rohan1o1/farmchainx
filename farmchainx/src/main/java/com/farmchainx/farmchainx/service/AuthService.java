package com.farmchainx.farmchainx.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.farmchainx.farmchainx.dto.RegisterRequest;
import com.farmchainx.farmchainx.dto.LoginRequest;
import com.farmchainx.farmchainx.model.Role;
import com.farmchainx.farmchainx.model.User;
import com.farmchainx.farmchainx.repository.RoleRepository;
import com.farmchainx.farmchainx.repository.UserRepository;

import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    
    public String register(RegisterRequest request) {
        try {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return "⚠️ Email already exists!";
            }

            String roleInput = request.getRole().toUpperCase();

            if (roleInput.equals("ADMIN") || roleInput.equals("ROLE_ADMIN")) {
                return "🚫 Cannot register as Admin!";
            }

        
            String chosenRole = roleInput.startsWith("ROLE_") ? roleInput : "ROLE_" + roleInput;

       
            Role userRole = roleRepository.findByRoleName(chosenRole)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + chosenRole));

            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRoles(Set.of(userRole));

            userRepository.save(user);

            return "✅ User registered successfully as " + chosenRole + "!";

        } catch (RuntimeException e) {
            e.printStackTrace();
            return "❌ Registration failed: " + e.getMessage();
        }
    }

    
    public String login(LoginRequest request) {
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Invalid email or password"));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid email or password");
            }

            return "✅ Login successful for " + user.getEmail();

        } catch (RuntimeException e) {
            e.printStackTrace();
            return "❌ Login failed: " + e.getMessage();
        }
    }
}
