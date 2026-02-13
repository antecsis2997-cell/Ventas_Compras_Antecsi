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

    @Operation(summary = "Ventas del día", description = "Total de ventas y monto para una fecha (formato yyyy-MM-dd).")
    @GetMapping("/ventas-dia")
    public ResponseEntity<DashboardVentasDTO> ventasDia(
            @Parameter(description = "Fecha en formato yyyy-MM-dd") @RequestParam String fecha) {
        return ResponseEntity.ok(service.ventasPorDia(LocalDate.parse(fecha)));
    }

    @Operation(summary = "Ventas del mes", description = "Total de ventas y monto para un año y mes.")
    @GetMapping("/ventas-mes")
    public ResponseEntity<DashboardVentasDTO> ventasMes(
            @Parameter(description = "Año (ej. 2026)") @RequestParam int year,
            @Parameter(description = "Mes (1-12)") @RequestParam int month) {
        return ResponseEntity.ok(service.ventasPorMes(year, month));
    }

    @Operation(summary = "Ventas del año", description = "Total de ventas y monto para un año.")
    @GetMapping("/ventas-anio")
    public ResponseEntity<DashboardVentasDTO> ventasAnio(
            @Parameter(description = "Año (ej. 2026)") @RequestParam int year) {
        return ResponseEntity.ok(service.ventasPorAnio(year));
    }

    @Operation(summary = "Producto más vendido", description = "Producto con mayor cantidad vendida (requiere ventas registradas).")
    @GetMapping("/producto-mas-vendido")
    public ResponseEntity<ProductoMasVendidoDTO> productoMasVendido() {
        return ResponseEntity.ok(service.productoMasVendido());
    }

    @Operation(summary = "Pedidos facturados y anulados", description = "Cantidad de pedidos con estado COMPLETADA (facturados) y ANULADA (anulados) en el mes. Dashboard Administración.")
    @GetMapping("/pedidos-estado")
    public ResponseEntity<DashboardPedidosEstadoDTO> pedidosEstado(
            @Parameter(description = "Año") @RequestParam int year,
            @Parameter(description = "Mes (1-12)") @RequestParam int month) {
        return ResponseEntity.ok(service.pedidosFacturadosYAnuladosPorMes(year, month));
    }
}
