package com.antecsis.controller;

import com.antecsis.dto.DashboardPedidosEstadoDTO;
import com.antecsis.dto.DashboardVentasDTO;
import com.antecsis.dto.producto.ProductoMasVendidoDTO;
import com.antecsis.service.DashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService service;

    @GetMapping("/ventas-dia")
    public ResponseEntity<DashboardVentasDTO> ventasDia(@RequestParam String fecha) {
        return ResponseEntity.ok(service.ventasPorDia(LocalDate.parse(fecha)));
    }

    @GetMapping("/ventas-mes")
    public ResponseEntity<DashboardVentasDTO> ventasMes(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(service.ventasPorMes(year, month));
    }

    @GetMapping("/ventas-anio")
    public ResponseEntity<DashboardVentasDTO> ventasAnio(@RequestParam int year) {
        return ResponseEntity.ok(service.ventasPorAnio(year));
    }

    @GetMapping("/producto-mas-vendido")
    public ResponseEntity<ProductoMasVendidoDTO> productoMasVendido() {
        return ResponseEntity.ok(service.productoMasVendido());
    }

    /** Dashboard Administración: pedidos facturados (COMPLETADA) y anulados (ANULADA) en el mes. */
    @GetMapping("/pedidos-estado")
    public ResponseEntity<DashboardPedidosEstadoDTO> pedidosEstado(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(service.pedidosFacturadosYAnuladosPorMes(year, month));
    }
}
