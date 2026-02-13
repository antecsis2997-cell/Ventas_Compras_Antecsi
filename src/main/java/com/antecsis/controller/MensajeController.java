package com.antecsis.controller;

import com.antecsis.dto.mensaje.MensajeRequestDTO;
import com.antecsis.dto.mensaje.MensajeResponseDTO;
import com.antecsis.service.MensajeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Mensajes", description = "CHAT: mensajes entre usuarios. Receptor, ITEM, descripción, precio, estado (EMERGENTE/LEVE).")
@RestController
@RequestMapping("/api/mensajes")
@RequiredArgsConstructor
public class MensajeController {

    private final MensajeService service;

    @PostMapping
    public ResponseEntity<MensajeResponseDTO> crear(@Valid @RequestBody MensajeRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @GetMapping
    public ResponseEntity<Page<MensajeResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @GetMapping("/receptor/{nombreReceptor}")
    public ResponseEntity<Page<MensajeResponseDTO>> porReceptor(
            @PathVariable String nombreReceptor, Pageable pageable) {
        return ResponseEntity.ok(service.listarPorReceptor(nombreReceptor, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MensajeResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }
}
