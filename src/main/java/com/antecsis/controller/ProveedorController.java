package com.antecsis.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antecsis.dto.proveedor.ProveedorRequestDTO;
import com.antecsis.dto.proveedor.ProveedorResponseDTO;
import com.antecsis.service.ProveedorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Proveedores", description = "CRUD de proveedores")
@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {
    private final ProveedorService proveedorService;

    @PostMapping
    public ResponseEntity<ProveedorResponseDTO> crear(@Valid @RequestBody ProveedorRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorService.crear(dto));
    }

    @GetMapping
    public ResponseEntity<Page<ProveedorResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(proveedorService.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(proveedorService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProveedorRequestDTO dto) {
        return ResponseEntity.ok(proveedorService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        proveedorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
