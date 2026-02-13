package com.antecsis.controller;

import com.antecsis.dto.InventarioResponseDTO;
import com.antecsis.service.InventarioService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Inventario", description = "Stock disponible y productos con stock bajo (alerta)")
@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {
    private final InventarioService service;

    @GetMapping
    public ResponseEntity<Page<InventarioResponseDTO>> listarTodo(Pageable pageable) {
        return ResponseEntity.ok(service.listarTodo(pageable));
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<Page<InventarioResponseDTO>> stockBajo(
            @RequestParam(defaultValue = "5") Integer limite,
            Pageable pageable) {
        return ResponseEntity.ok(service.stockBajo(limite, pageable));
    }
}
