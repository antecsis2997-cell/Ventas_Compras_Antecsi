package com.antecsis.controller;

import com.antecsis.dto.venta.VentaRequestDTO;
import com.antecsis.dto.venta.VentaResponseDTO;
import com.antecsis.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {
    private final VentaService service;

    public VentaController(VentaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<VentaResponseDTO> crear(
            @Valid @RequestBody VentaRequestDTO dto) {
        return ResponseEntity.ok(service.crear(dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }
}
