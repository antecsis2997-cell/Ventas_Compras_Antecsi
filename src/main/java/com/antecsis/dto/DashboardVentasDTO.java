package com.antecsis.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resumen de ventas con datos agregados para gráficos.
 * <p>
 * Los campos de gráfico son opcionales para clientes que solo necesiten totalVentas/montoTotal.
 */
public record DashboardVentasDTO(
        Long totalVentas,
        BigDecimal montoTotal,
        List<String> graficoLabels,
        List<Double> graficoValores,
        Long completadas,
        Long anuladas,
        Long pendientes
) {}
