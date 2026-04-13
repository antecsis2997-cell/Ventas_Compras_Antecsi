package com.antecsis.controller;

import java.util.List;

import com.antecsis.dto.sector.SectorActivoRequestDTO;
import com.antecsis.dto.sector.SectorPlataformaDTO;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Sectores", description = "Sectores/sedes. Alta/baja: SUPERADMIN. Lectura: SUPERADMIN, SUPERUSUARIO cliente, ADMIN.")
@RestController
@RequestMapping("/api/sectores")
@RequiredArgsConstructor
public class SectorController {

    private final SectorService service;

    @io.swagger.v3.oas.annotations.Operation(summary = "Sedes para la plataforma principal (todas si es superusuario, solo la suya si no)")
    @GetMapping("/plataforma")
    public ResponseEntity<List<SectorPlataformaDTO>> plataforma() {
        return ResponseEntity.ok(service.listarParaPlataforma());
    }

    @PreAuthorize("hasRole('SUPERADMIN')")
    @PostMapping
    public ResponseEntity<SectorResponseDTO> crear(@Valid @RequestBody SectorRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PreAuthorize("hasAnyRole('SUPERADMIN','SUPERUSUARIO','ADMIN')")
    @GetMapping
    public ResponseEntity<Page<SectorResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @PreAuthorize("hasAnyRole('SUPERADMIN','SUPERUSUARIO','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<SectorResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PreAuthorize("hasRole('SUPERADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<SectorResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SectorRequestDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @PreAuthorize("hasRole('SUPERADMIN')")
    @PatchMapping("/{id}/activo")
    public ResponseEntity<SectorResponseDTO> cambiarActivo(
            @PathVariable Long id,
            @Valid @RequestBody SectorActivoRequestDTO dto) {
        return ResponseEntity.ok(service.cambiarActivo(id, dto));
    }

    @PreAuthorize("hasRole('SUPERADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
