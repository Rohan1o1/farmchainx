package com.farmchainx.farmchainx.service;

import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.farmchainx.farmchainx.dto.RegisterRequest;
import com.farmchainx.farmchainx.model.Role;
import com.farmchainx.farmchainx.model.User;
import com.farmchainx.farmchainx.repository.RoleRepository;
import com.farmchainx.farmchainx.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
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
    
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            return "Email already exists!";
        }

        
        Role userRole = roleRepository.findByRoleName("ROLE_USER")
                         .orElseThrow(() -> new RuntimeException("Role not found"));

   
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); 
        user.setRoles(Set.of(userRole)); 
   
        userRepository.save(user);

        return "User registered successfully!";
    }
}

