package com.antecsis.controller;

import com.antecsis.dto.suscripcion.CompraPublicaRequestDTO;
import com.antecsis.dto.suscripcion.SuscripcionRequestDTO;
import com.antecsis.dto.suscripcion.SuscripcionResponseDTO;
import com.antecsis.service.SuscripcionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Suscripciones", description = "Administrador de suscripciones/licencias. Solo SUPERUSUARIO.")
@RestController
@RequestMapping("/api/suscripciones")
@RequiredArgsConstructor
public class SuscripcionController {

    private final SuscripcionService service;

    @PreAuthorize("hasRole('SUPERUSUARIO')")
    @Operation(summary = "Crear suscripción")
    @PostMapping
    public ResponseEntity<SuscripcionResponseDTO> crear(@Valid @RequestBody SuscripcionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PreAuthorize("hasRole('SUPERUSUARIO')")
    @Operation(summary = "Listar suscripciones")
    @GetMapping
    public ResponseEntity<Page<SuscripcionResponseDTO>> listar(
            Pageable pageable,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long rubroId) {
        return ResponseEntity.ok(service.listar(pageable, estado, rubroId));
    }

    @PreAuthorize("hasRole('SUPERUSUARIO')")
    @Operation(summary = "Obtener suscripción por ID")
    @GetMapping("/{id}")
    public ResponseEntity<SuscripcionResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PreAuthorize("hasRole('SUPERUSUARIO')")
    @Operation(summary = "Actualizar suscripción")
    @PutMapping("/{id}")
    public ResponseEntity<SuscripcionResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SuscripcionRequestDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @PreAuthorize("hasRole('SUPERUSUARIO')")
    @Operation(summary = "Eliminar suscripción")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('SUPERUSUARIO')")
    @Operation(summary = "Enviar alerta de suscripción vencida")
    @PostMapping("/{id}/enviar-alerta")
    public ResponseEntity<Void> enviarAlerta(@PathVariable Long id) {
        service.enviarAlerta(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Compra pública de plan (obtener el programa)", description = "No requiere autenticación. Simula pago y crea suscripción PAGADA.")
    @PostMapping("/compra-publica")
    public ResponseEntity<Void> compraPublica(@Valid @RequestBody CompraPublicaRequestDTO dto) {
        service.compraPublica(
                dto.plan(),
                dto.ruc(),
                dto.nombreCliente(),
                dto.correoAdministrador(),
                dto.rubroCodigo(),
                dto.nombreTitularTarjeta(),
                dto.numeroTarjeta(),
                dto.fechaCaducidadTarjeta(),
                dto.sectorId()
        );
        return ResponseEntity.noContent().build();
    }
}
