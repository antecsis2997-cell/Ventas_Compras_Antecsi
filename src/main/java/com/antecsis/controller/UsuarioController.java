package com.antecsis.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antecsis.dto.usuario.UsuarioCreateRequest;
import com.antecsis.dto.usuario.UsuarioResponseDTO;
import com.antecsis.dto.usuario.UsuarioUpdateRequest;
import com.antecsis.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Tag(name = "Usuarios", description = "Listado y creación de usuarios. Solo SUPERUSUARIO o ADMIN.")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;

    @PreAuthorize("hasAnyAuthority('ROLE_SUPERUSUARIO','ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UsuarioResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(usuarioService.listar(pageable));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPERUSUARIO','ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPERUSUARIO','ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<Map<String, Object>> crearUsuario(@RequestBody @Valid UsuarioCreateRequest dto) {
        usuarioService.crearUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("success", true, "message", "Usuario creado correctamente")
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPERUSUARIO','ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizar(@PathVariable Long id, @RequestBody UsuarioUpdateRequest dto) {
        return ResponseEntity.ok(usuarioService.actualizar(id, dto));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPERUSUARIO','ROLE_ADMIN')")
    @PatchMapping("/{id}/activo")
    public ResponseEntity<Void> actualizarActivo(@PathVariable Long id, @RequestBody java.util.Map<String, Boolean> body) {
        Boolean activo = body != null ? body.get("activo") : null;
        if (activo == null) {
            return ResponseEntity.badRequest().build();
        }
        usuarioService.actualizarActivo(id, activo);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPERUSUARIO','ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
