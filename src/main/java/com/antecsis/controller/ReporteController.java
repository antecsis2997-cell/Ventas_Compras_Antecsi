package com.antecsis.controller;

import com.antecsis.service.ReporteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;

@Tag(name = "Reportes", description = "Exportación de ventas por rango de fechas: Excel (.xlsx) y PDF")
@Slf4j
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @Operation(summary = "Exportar ventas a Excel", description = "Descarga un archivo .xlsx con las ventas entre fechaInicio y fechaFin.")
    @GetMapping("/ventas-excel")
    public ResponseEntity<byte[]> ventasExcel(
            @Parameter(description = "Fecha inicio (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            byte[] bytes = reporteService.exportarVentasExcel(fechaInicio, fechaFin);
            String filename = "ventas_" + fechaInicio + "_" + fechaFin + ".xlsx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(bytes.length)
                    .body(bytes);
        } catch (Exception e) {
            log.error("Error al exportar ventas a Excel: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Exportar ventas a PDF", description = "Descarga un archivo PDF con las ventas entre fechaInicio y fechaFin.")
    @GetMapping("/ventas-pdf")
    public ResponseEntity<byte[]> ventasPdf(
            @Parameter(description = "Fecha inicio (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            byte[] bytes = reporteService.exportarVentasPdf(fechaInicio, fechaFin);
            String filename = "ventas_" + fechaInicio + "_" + fechaFin + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(bytes.length)
                    .body(bytes);
        } catch (Exception e) {
            log.error("Error al exportar ventas a PDF: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
