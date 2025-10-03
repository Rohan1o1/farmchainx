package com.farmchainx.farmchainx.service;

import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.farmchainx.farmchainx.dto.RegisterRequest;
import com.farmchainx.farmchainx.model.Role;
import com.farmchainx.farmchainx.model.User;
import com.farmchainx.farmchainx.repository.RoleRepository;
import com.farmchainx.farmchainx.repository.UserRepository;

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

    public String register(RegisterRequest register) {

        //git testing update
        if (userRepository.findByEmail(register.getEmail()).isPresent()) {
            return "Email is already taken";
        }

       
        String chosenRole = register.getRole().toUpperCase();

        
        if (chosenRole.equals("ADMIN")) {
            return "Cannot register as Admin. Contact system administrator.";
        }

      
        String roleName = "ROLE_" + chosenRole;

        
        Role userRole = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        
        User user = new User();
        user.setName(register.getName());
        user.setEmail(register.getEmail());
        user.setPassword(passwordEncoder.encode(register.getPassword()));
        user.setRoles(Set.of(userRole));

     
        userRepository.save(user);

        return "User registered successfully as " + chosenRole;
    }
}
