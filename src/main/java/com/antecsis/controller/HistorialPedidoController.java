package com.antecsis.controller;

import com.antecsis.dto.historial.HistorialPedidoResponseDTO;
import com.antecsis.service.HistorialPedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

/** Historial_Pedidos: consulta rápida para reportes (documento: LOAD 1ms). */
@Tag(name = "Historial pedidos", description = "Historial de ventas por producto y por rango de fechas")
@RestController
@RequestMapping("/api/historial-pedidos")
@RequiredArgsConstructor
public class HistorialPedidoController {

    private final HistorialPedidoService service;

    @GetMapping
    public ResponseEntity<Page<HistorialPedidoResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @GetMapping("/por-fechas")
    public ResponseEntity<List<HistorialPedidoResponseDTO>> porFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(service.listarPorFechas(fechaInicio, fechaFin));
    }
}
