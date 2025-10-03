package com.farmchainx.farmchainx.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmchainx.farmchainx.dto.RegisterRequest;
import com.farmchainx.farmchainx.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	private final AuthService authService;
	
	public AuthController(AuthService authService) {
		this.authService = authService;
	}
	
	@PostMapping("/register")
	public ResponseEntity<String> register(@RequestBody RegisterRequest register){
		return ResponseEntity.ok(authService.register(register));
		
	}

}
