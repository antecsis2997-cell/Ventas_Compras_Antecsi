package com.antecsis.dto;

import java.math.BigDecimal;

public record DashboardVentasDTO(
    Long totalVentas,
    BigDecimal montoTotal
) {}
