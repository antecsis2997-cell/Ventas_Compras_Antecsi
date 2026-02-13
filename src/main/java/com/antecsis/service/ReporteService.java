package com.antecsis.service;

import java.time.LocalDate;

/**
 * Reportes Excel/PDF (documento: REPORT - LIBRERIA, EXCEL, PDF).
 */
public interface ReporteService {
    byte[] exportarVentasExcel(LocalDate fechaInicio, LocalDate fechaFin) throws Exception;
    byte[] exportarVentasPdf(LocalDate fechaInicio, LocalDate fechaFin) throws Exception;
}
