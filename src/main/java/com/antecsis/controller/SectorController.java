package com.antecsis.controller;

import com.antecsis.dto.sector.SectorRequestDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.antecsis.dto.sector.SectorResponseDTO;
import com.antecsis.service.SectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Sectores", description = "Sectores/sedes. Solo SUPERUSUARIO.")
@RestController
@RequestMapping("/api/sectores")
@RequiredArgsConstructor
public class SectorController {

    private final SectorService service;

    @PreAuthorize("hasRole('SUPERUSUARIO')")
    @PostMapping
    public ResponseEntity<SectorResponseDTO> crear(@Valid @RequestBody SectorRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN')")
    @GetMapping
    public ResponseEntity<Page<SectorResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<SectorResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PreAuthorize("hasRole('SUPERUSUARIO')")
    @PutMapping("/{id}")
    public ResponseEntity<SectorResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SectorRequestDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @PreAuthorize("hasRole('SUPERUSUARIO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
