package com.antecsis.controller;

import com.antecsis.dto.venta.VentaRequestDTO;
import com.antecsis.dto.venta.VentaResponseDTO;
import com.antecsis.service.VentaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Ventas", description = "Registro de ventas, listado, anulación (Factura/Boleta)")
@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {
    private final VentaService service;

    @Operation(summary = "Registrar venta", description = "Crea una venta con cliente, items (productoId, cantidad), tipo documento (FACTURA/BOLETA) y número. Requiere rol CAJERO, ADMIN o SUPERUSUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venta creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o stock insuficiente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permiso")
    })
    @PostMapping
    public ResponseEntity<VentaResponseDTO> crear(@Valid @RequestBody VentaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @Operation(summary = "Listar ventas", description = "Listado paginado. Requiere CAJERO, ADMIN o SUPERUSUARIO.")
    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN','CAJERO')")
    @GetMapping
    public ResponseEntity<Page<VentaResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @Operation(summary = "Obtener venta por ID")
    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> obtener(@Parameter(description = "ID de la venta") @PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @Operation(summary = "Anular venta", description = "Cambia el estado a ANULADA. Solo SUPERUSUARIO o ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta anulada"),
            @ApiResponse(responseCode = "400", description = "Venta ya anulada"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada")
    })
    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN')")
    @PatchMapping("/{id}/anular")
    public ResponseEntity<VentaResponseDTO> anular(@Parameter(description = "ID de la venta") @PathVariable Long id) {
        return ResponseEntity.ok(service.anular(id));
    }
}
