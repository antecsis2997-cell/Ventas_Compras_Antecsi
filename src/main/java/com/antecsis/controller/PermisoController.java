package com.antecsis.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antecsis.dto.permiso.ModuloDTO;
import com.antecsis.dto.permiso.PermisoUpdateRequest;
import com.antecsis.service.PermisoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Permisos", description = "Gestión de permisos/módulos por usuario")
@RestController
@RequestMapping("/api/permisos")
@RequiredArgsConstructor
public class PermisoController {

    private final PermisoService permisoService;

    @Operation(summary = "Listar todos los módulos del sistema",
            description = "Devuelve el catálogo de módulos disponibles. Cualquier usuario autenticado.")
    @GetMapping("/modulos")
    public ResponseEntity<List<ModuloDTO>> listarModulos() {
        return ResponseEntity.ok(permisoService.listarModulos());
    }

    @Operation(summary = "Obtener permisos de un usuario",
            description = "Lista todos los módulos indicando cuáles tiene asignados. Solo SUPERUSUARIO o ADMIN.")
    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN')")
    @GetMapping("/usuarios/{usuarioId}")
    public ResponseEntity<List<ModuloDTO>> obtenerPermisosUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(permisoService.obtenerPermisosUsuario(usuarioId));
    }

    @Operation(summary = "Actualizar permisos de un usuario",
            description = "Reemplaza los módulos asignados al usuario. Enviar los códigos de módulos que debe tener. Solo SUPERUSUARIO o ADMIN.")
    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN')")
    @PutMapping("/usuarios/{usuarioId}")
    public ResponseEntity<Map<String, Object>> actualizarPermisos(
            @PathVariable Long usuarioId,
            @RequestBody @Valid PermisoUpdateRequest request) {
        permisoService.actualizarPermisosUsuario(usuarioId, request.moduloCodigos());
        return ResponseEntity.ok(Map.of("success", true, "message", "Permisos actualizados correctamente"));
    }
}
