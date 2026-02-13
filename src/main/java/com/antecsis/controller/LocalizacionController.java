package com.antecsis.controller;

import com.antecsis.dto.localizacion.LocalizacionRequestDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.antecsis.dto.localizacion.LocalizacionResponseDTO;
import com.antecsis.service.LocalizacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Localizaciones", description = "Localizaciones: descripción e imagen (jpg, png, webp)")
@RestController
@RequestMapping("/api/localizaciones")
@RequiredArgsConstructor
public class LocalizacionController {

    private final LocalizacionService service;

    @PostMapping
    public ResponseEntity<LocalizacionResponseDTO> crear(@Valid @RequestBody LocalizacionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @GetMapping
    public ResponseEntity<Page<LocalizacionResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocalizacionResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocalizacionResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody LocalizacionRequestDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
