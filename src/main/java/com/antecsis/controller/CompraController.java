package com.antecsis.controller;

import com.antecsis.dto.compra.CompraRequestDTO;
import com.antecsis.dto.compra.CompraResponseDTO;
import com.antecsis.service.CompraService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {
    private final CompraService service;

    @PostMapping
    public ResponseEntity<CompraResponseDTO> crear(@Valid @RequestBody CompraRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @GetMapping
    public ResponseEntity<Page<CompraResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

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
