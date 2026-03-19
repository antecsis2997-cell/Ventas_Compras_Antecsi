package com.antecsis.controller;

import com.antecsis.dto.receta.RecetaRequestDTO;
import com.antecsis.dto.receta.RecetaResponseDTO;
import com.antecsis.service.RecetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Recetas", description = "Recetas (BOM) para convertir insumos en productos")
@RestController
@RequestMapping("/api/recetas")
@RequiredArgsConstructor
public class RecetaController {

    private final RecetaService service;

    @Operation(summary = "Crear receta", description = "Define una receta consumiendo insumos para producir un producto (escala proporcional).")
    @PostMapping
    public ResponseEntity<RecetaResponseDTO> crear(@Valid @RequestBody RecetaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @Operation(summary = "Listar recetas", description = "Lista recetas de la bodega del usuario autenticado.")
    @GetMapping
    public ResponseEntity<Page<RecetaResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @Operation(summary = "Obtener receta", description = "Obtiene una receta por ID.")
    @GetMapping("/{id}")
    public ResponseEntity<RecetaResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }
}

