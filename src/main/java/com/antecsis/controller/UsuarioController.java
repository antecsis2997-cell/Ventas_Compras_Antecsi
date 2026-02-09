package com.antecsis.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antecsis.dto.usuario.UsuarioCreateRequest;
import com.antecsis.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
	private final UsuarioService usuarioService;

	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<?> crearUsuario(@RequestBody @Valid UsuarioCreateRequest dto) {
        usuarioService.crearUsuario(dto);
        return ResponseEntity.ok("Usuario creado correctamente");
    }
    
}
