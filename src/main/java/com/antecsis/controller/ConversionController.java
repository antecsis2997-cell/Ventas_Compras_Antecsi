package com.antecsis.controller;

import com.antecsis.dto.conversion.ConversionRequestDTO;
import com.antecsis.dto.conversion.ConversionResponseDTO;
import com.antecsis.service.ConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Conversiones", description = "Conversiones de insumos en productos (recetas/BOM)")
@RestController
@RequestMapping("/api/conversiones")
@RequiredArgsConstructor
public class ConversionController {

    private final ConversionService service;

    @Operation(summary = "Convertir insumos en producto", description = "Usa una receta para consumir insumos (sin fracciones) y producir varias unidades del producto de salida.")
    @PostMapping
    public ResponseEntity<ConversionResponseDTO> convertir(@Valid @RequestBody ConversionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.convertir(dto));
    }
}

