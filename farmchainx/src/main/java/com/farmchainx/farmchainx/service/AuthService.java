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

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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
    
    public AuthResponse login(LoginRequest login) {
    	
    	User user = userRepository.findByEmail(login.getEmail())
    			.orElseThrow(()-> new RuntimeException("User not found"));
    	
    	if(!passwordEncoder.matches(login.getPassword(), user.getPassword())) {
    		throw new RuntimeException("Invalid Password");
    	}
    	
    	String token = jwtUtil.generateToken(user.getEmail());
    	
    	String role = user.getRoles().iterator().next().getRoleName();
    	
    	return new AuthResponse(token, role, user.getEmail());
    	
    
    }

    
 
}
