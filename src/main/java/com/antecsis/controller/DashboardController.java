package com.antecsis.controller;

import com.antecsis.dto.DashboardPedidosEstadoDTO;
import com.antecsis.dto.DashboardVentasDTO;
import com.antecsis.dto.producto.ProductoMasVendidoDTO;
import com.antecsis.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Dashboard", description = "Resumen ventas día/mes/año, producto más vendido, pedidos facturados/anulados")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService service;

    @Operation(summary = "Ventas del día")
    @GetMapping("/ventas-dia")
    public ResponseEntity<DashboardVentasDTO> ventasDia(
            @Parameter(description = "Fecha en formato yyyy-MM-dd") @RequestParam String fecha,
            @Parameter(description = "ID del sector (opcional)") @RequestParam(required = false) Long sectorId) {
        return ResponseEntity.ok(service.ventasPorDia(LocalDate.parse(fecha), sectorId));
    }

    @Operation(summary = "Ventas del mes")
    @GetMapping("/ventas-mes")
    public ResponseEntity<DashboardVentasDTO> ventasMes(
            @Parameter(description = "Año (ej. 2026)") @RequestParam int year,
            @Parameter(description = "Mes (1-12)") @RequestParam int month,
            @Parameter(description = "ID del sector (opcional)") @RequestParam(required = false) Long sectorId) {
        return ResponseEntity.ok(service.ventasPorMes(year, month, sectorId));
    }

    @Operation(summary = "Ventas del año")
    @GetMapping("/ventas-anio")
    public ResponseEntity<DashboardVentasDTO> ventasAnio(
            @Parameter(description = "Año (ej. 2026)") @RequestParam int year,
            @Parameter(description = "ID del sector (opcional)") @RequestParam(required = false) Long sectorId) {
        return ResponseEntity.ok(service.ventasPorAnio(year, sectorId));
    }

    @Operation(summary = "Producto más vendido")
    @GetMapping("/producto-mas-vendido")
    public ResponseEntity<ProductoMasVendidoDTO> productoMasVendido(
            @Parameter(description = "ID del sector (opcional)") @RequestParam(required = false) Long sectorId) {
        return ResponseEntity.ok(service.productoMasVendido(sectorId));
    }

    @Operation(summary = "Pedidos facturados y anulados en el mes")
    @GetMapping("/pedidos-estado")
    public ResponseEntity<DashboardPedidosEstadoDTO> pedidosEstado(
            @Parameter(description = "Año") @RequestParam int year,
            @Parameter(description = "Mes (1-12)") @RequestParam int month,
            @Parameter(description = "ID del sector (opcional)") @RequestParam(required = false) Long sectorId) {
        return ResponseEntity.ok(service.pedidosFacturadosYAnuladosPorMes(year, month, sectorId));
    }
}
