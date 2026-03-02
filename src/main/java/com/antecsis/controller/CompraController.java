package com.antecsis.controller;

import com.antecsis.dto.compra.CompraRequestDTO;
import com.antecsis.dto.compra.CompraResponseDTO;
import com.antecsis.service.CompraService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Compras", description = "Registro de compras, listado y anulación")
@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {
    private final CompraService service;

    @Operation(summary = "Registrar compra", description = "Crea una compra. El sector se asigna automáticamente desde la sede del usuario.")
    @PostMapping
    public ResponseEntity<CompraResponseDTO> crear(@Valid @RequestBody CompraRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @Operation(summary = "Listar compras", description = "Listado paginado. Opcionalmente filtra por sector.")
    @GetMapping
    public ResponseEntity<Page<CompraResponseDTO>> listar(
            Pageable pageable,
            @Parameter(description = "ID del sector (opcional)") @RequestParam(required = false) Long sectorId) {
        return ResponseEntity.ok(service.listar(pageable, sectorId));
    }

    @Operation(summary = "Obtener compra por ID")
    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN','CAJERO')")
    @GetMapping("/{id}")
    public ResponseEntity<CompraResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN')")
    @PatchMapping("/{id}/anular")
    public ResponseEntity<CompraResponseDTO> anular(@PathVariable Long id) {
        return ResponseEntity.ok(service.anular(id));
    }
}
