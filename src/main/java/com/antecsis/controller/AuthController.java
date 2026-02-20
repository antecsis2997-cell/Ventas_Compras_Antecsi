package com.antecsis.controller;

import com.antecsis.dto.login.LoginRequestDTO;
import com.antecsis.dto.login.LoginResponseDTO;
import com.antecsis.dto.login.MeResponseDTO;
import com.antecsis.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Login y autenticación JWT")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @Operation(summary = "Iniciar sesión", description = "Devuelve un token JWT para usar en el header Authorization: Bearer <token>. No requiere autenticación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto", content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Usuario o contraseña incorrectos"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (username/password vacíos)")
    })
    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO dto) {
        String token = service.login(dto.getUsername(), dto.getPassword());
        return new LoginResponseDTO(token);
    }

    @Operation(summary = "Usuario actual", description = "Devuelve el usuario autenticado (rol y sede). Requiere Bearer token.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = MeResponseDTO.class)))
    @GetMapping("/me")
    public ResponseEntity<MeResponseDTO> me() {
        return ResponseEntity.ok(service.getCurrentUser());
    }
}
