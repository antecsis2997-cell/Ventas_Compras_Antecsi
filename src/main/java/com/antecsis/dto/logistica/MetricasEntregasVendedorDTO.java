package com.antecsis.dto.logistica;

import java.math.BigDecimal;

public record MetricasEntregasVendedorDTO(
    String vendedorNombre,
    Long vendedorId,
    long cantidadEntregas,
    BigDecimal montoTotal
) {}
