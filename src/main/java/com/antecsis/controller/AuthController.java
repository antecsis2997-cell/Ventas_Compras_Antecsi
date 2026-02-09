package com.antecsis.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antecsis.dto.login.LoginRequestDTO;
import com.antecsis.dto.login.LoginResponseDTO;
import com.antecsis.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }
    
    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO dto) {
    	String token = service.login(dto.getUsername(), dto.getPassword());
    	return new LoginResponseDTO(token);
    }
}
