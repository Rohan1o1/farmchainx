package com.farmchainx.farmchainx.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmchainx.farmchainx.dto.AuthResponse;
import com.farmchainx.farmchainx.dto.LoginRequest;
import com.farmchainx.farmchainx.dto.RegisterRequest;
import com.farmchainx.farmchainx.repository.UserRepository;
import com.farmchainx.farmchainx.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
	
	private final AuthService authService;
	
	public AuthController(AuthService authService, UserRepository userRepository) {
		this.authService = authService;
		this.userRepository = userRepository;
	}
	
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody RegisterRequest register) {
	    if (userRepository.existsByEmail(register.getEmail())) {
	        return ResponseEntity.badRequest().body(
	            Map.of("error", "Email already exists!")
	        );
	    }

	    AuthResponse response = authService.register(register);
	    return ResponseEntity.ok(response);
	}



	
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest login){
		return ResponseEntity.ok(authService.login(login));
	}

}
