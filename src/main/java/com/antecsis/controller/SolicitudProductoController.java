package com.antecsis.controller;

import com.antecsis.dto.solicitud.SolicitudProductoRequestDTO;
import com.antecsis.dto.solicitud.SolicitudProductoResponseDTO;
import com.antecsis.service.SolicitudProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Solicitudes producto", description = "Solicitudes de producto (Logística). Estado EMERGENTE (rojo) o LEVE (amarillo).")
@RestController
@RequestMapping("/api/solicitudes-producto")
@RequiredArgsConstructor
public class SolicitudProductoController {

    private final SolicitudProductoService service;

    @PostMapping
    public ResponseEntity<SolicitudProductoResponseDTO> crear(@Valid @RequestBody SolicitudProductoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @GetMapping
    public ResponseEntity<Page<SolicitudProductoResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<SolicitudProductoResponseDTO>> pendientes() {
        return ResponseEntity.ok(service.listarPendientes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudProductoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PatchMapping("/{id}/atendida")
    public ResponseEntity<SolicitudProductoResponseDTO> marcarAtendida(@PathVariable Long id) {
        return ResponseEntity.ok(service.marcarAtendida(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
