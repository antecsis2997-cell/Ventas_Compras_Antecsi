package com.antecsis.controller;

import com.antecsis.dto.solicitudstock.SolicitudStockRequestDTO;
import com.antecsis.dto.solicitudstock.SolicitudStockResponseDTO;
import com.antecsis.dto.usuario.UsuarioCorreoDTO;
import com.antecsis.service.SolicitudStockService;
import com.antecsis.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Solicitudes Stock", description = "Solicitudes de adquisición de stock. Cajero envía, Logística aprueba/desaprueba.")
@RestController
@RequestMapping("/api/solicitudes-stock")
@RequiredArgsConstructor
public class SolicitudStockController {

    private final SolicitudStockService service;
    private final UsuarioService usuarioService;

    @PreAuthorize("hasAnyRole('SUPERADMIN','SUPERUSUARIO','ADMIN','CAJERO')")
    @PostMapping
    public ResponseEntity<SolicitudStockResponseDTO> crear(@Valid @RequestBody SolicitudStockRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<Page<SolicitudStockResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @PreAuthorize("hasAnyRole('SUPERADMIN','SUPERUSUARIO','ADMIN','LOGISTICA')")
    @PostMapping("/{id}/aprobar")
    public ResponseEntity<SolicitudStockResponseDTO> aprobar(@PathVariable Long id) {
        return ResponseEntity.ok(service.aprobar(id));
    }

    @PreAuthorize("hasAnyRole('SUPERADMIN','SUPERUSUARIO','ADMIN','LOGISTICA')")
    @PostMapping("/{id}/desaprobar")
    public ResponseEntity<SolicitudStockResponseDTO> desaprobar(@PathVariable Long id) {
        return ResponseEntity.ok(service.desaprobar(id));
    }

    @PreAuthorize("hasAnyRole('SUPERADMIN','SUPERUSUARIO','ADMIN','CAJERO')")
    @GetMapping("/usuarios-por-correo")
    public ResponseEntity<List<UsuarioCorreoDTO>> usuariosPorCorreo(@RequestParam String q) {
        return ResponseEntity.ok(usuarioService.buscarPorCorreo(q));
    }
}
