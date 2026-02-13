package com.antecsis.controller;

import com.antecsis.dto.venta.VentaRequestDTO;
import com.antecsis.dto.venta.VentaResponseDTO;
import com.antecsis.service.VentaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {
    private final VentaService service;

    @PostMapping
    public ResponseEntity<VentaResponseDTO> crear(@Valid @RequestBody VentaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN','CAJERO')")
    @GetMapping
    public ResponseEntity<Page<VentaResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN')")
    @PatchMapping("/{id}/anular")
    public ResponseEntity<VentaResponseDTO> anular(@PathVariable Long id) {
        return ResponseEntity.ok(service.anular(id));
    }
}
