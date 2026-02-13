package com.antecsis.controller;

import org.springframework.http.HttpStatus;
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

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;

    @PreAuthorize("hasAnyAuthority('ROLE_SUPERUSUARIO','ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<Map<String, Object>> crearUsuario(@RequestBody @Valid UsuarioCreateRequest dto) {
        usuarioService.crearUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("success", true, "message", "Usuario creado correctamente")
        );
    }
}
