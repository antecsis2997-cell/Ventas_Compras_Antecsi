package com.antecsis.controller;

import com.antecsis.dto.InventarioResponseDTO;
import com.antecsis.dto.inventario.AjusteStockRequestDTO;
import com.antecsis.dto.inventario.MovimientoResponseDTO;
import com.antecsis.service.InventarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Inventario", description = "Stock, movimientos y ajustes de inventario.")
@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {
    private final InventarioService service;

    @Operation(summary = "Listar inventario")
    @GetMapping
    public ResponseEntity<Page<InventarioResponseDTO>> listarTodo(
            Pageable pageable,
            @Parameter(description = "ID del sector (opcional)") @RequestParam(required = false) Long sectorId) {
        return ResponseEntity.ok(service.listarTodo(pageable, sectorId));
    }

    @Operation(summary = "Productos con stock bajo (por umbral global)")
    @GetMapping("/stock-bajo")
    public ResponseEntity<Page<InventarioResponseDTO>> stockBajo(
            @RequestParam(defaultValue = "5") Integer limite,
            Pageable pageable,
            @Parameter(description = "ID del sector (opcional)") @RequestParam(required = false) Long sectorId) {
        return ResponseEntity.ok(service.stockBajo(limite, pageable, sectorId));
    }

    @Operation(summary = "Productos con stock bajo (por stock_minimo_alerta variable por producto)")
    @GetMapping("/stock-bajo-alerta")
    public ResponseEntity<Page<InventarioResponseDTO>> stockBajoPorAlerta(
            Pageable pageable,
            @Parameter(description = "ID del sector (opcional)") @RequestParam(required = false) Long sectorId) {
        return ResponseEntity.ok(service.stockBajoPorAlerta(pageable, sectorId));
    }

    @Operation(summary = "Insumos (materiales) en inventario")
    @GetMapping("/insumos")
    public ResponseEntity<Page<InventarioResponseDTO>> listarInsumosTodo(
            Pageable pageable,
            @Parameter(description = "ID del sector (opcional)") @RequestParam(required = false) Long sectorId) {
        return ResponseEntity.ok(service.listarInsumosTodo(pageable, sectorId));
    }

    @Operation(summary = "Insumos con stock bajo (por umbral global)")
    @GetMapping("/insumos/stock-bajo")
    public ResponseEntity<Page<InventarioResponseDTO>> stockBajoInsumos(
            @RequestParam(defaultValue = "5") Integer limite,
            Pageable pageable,
            @Parameter(description = "ID del sector (opcional)") @RequestParam(required = false) Long sectorId) {
        return ResponseEntity.ok(service.stockBajoInsumos(limite, pageable, sectorId));
    }

    @Operation(summary = "Historial de movimientos", description = "Movimientos de inventario ordenados por fecha descendente. Filtrable por producto.")
    @GetMapping("/movimientos")
    public ResponseEntity<Page<MovimientoResponseDTO>> movimientos(
            @PageableDefault(sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Filtrar por producto (opcional)") @RequestParam(required = false) Long productoId) {
        return ResponseEntity.ok(service.listarMovimientos(pageable, productoId));
    }

    @Operation(summary = "Ajustar stock manualmente", description = "Corrección de stock por conteo físico o ajuste.")
    @PostMapping("/ajuste")
    public ResponseEntity<MovimientoResponseDTO> ajustarStock(@Valid @RequestBody AjusteStockRequestDTO dto) {
        return ResponseEntity.ok(service.ajustarStock(dto));
    }
}
