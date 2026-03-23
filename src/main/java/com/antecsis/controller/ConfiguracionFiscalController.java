package com.antecsis.controller;

import com.antecsis.dto.sunat.ConfiguracionFiscalRequestDTO;
import com.antecsis.dto.sunat.ConfiguracionFiscalResponseDTO;
import com.antecsis.service.ConfiguracionFiscalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Configuración Fiscal", description = "Gestión de configuración SUNAT por bodega (SEE del Contribuyente)")
@RestController
@RequestMapping("/api/configuracion-fiscal")
@RequiredArgsConstructor
public class ConfiguracionFiscalController {

    private final ConfiguracionFiscalService service;

    /**
     * SUPERUSUARIO → lista todas las bodegas.
     * ADMIN        → lista solo su propia bodega.
     */
    @Operation(summary = "Listar configuraciones fiscales según rol")
    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN')")
    @GetMapping
    public ResponseEntity<List<ConfiguracionFiscalResponseDTO>> listar(Authentication auth) {
        return ResponseEntity.ok(service.listarParaUsuario(auth.getName()));
    }

    /**
     * SUPERUSUARIO → puede guardar para cualquier sector.
     * ADMIN        → solo puede guardar para su propio sector (validado en el servicio).
     */
    @Operation(summary = "Crear o actualizar configuración fiscal")
    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN')")
    @PostMapping
    public ResponseEntity<ConfiguracionFiscalResponseDTO> guardar(
            @Valid @RequestBody ConfiguracionFiscalRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.ok(service.guardarParaUsuario(dto, auth.getName()));
    }

    /**
     * SUPERUSUARIO → activa cualquier configuración.
     * ADMIN        → solo puede activar la de su propia bodega.
     */
    @Operation(summary = "Activar configuración fiscal")
    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN')")
    @PatchMapping("/{id}/activar")
    public ResponseEntity<ConfiguracionFiscalResponseDTO> activar(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(service.activarParaUsuario(id, auth.getName()));
    }

    /**
     * SUPERUSUARIO → desactiva cualquier configuración.
     * ADMIN        → solo puede desactivar la de su propia bodega.
     */
    @Operation(summary = "Desactivar configuración fiscal")
    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN')")
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ConfiguracionFiscalResponseDTO> desactivar(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(service.desactivarParaUsuario(id, auth.getName()));
    }
}
