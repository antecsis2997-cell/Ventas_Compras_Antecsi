package com.antecsis.dto.logistica;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetricasEntregasVendedorDTO {
    private String vendedorNombre;
    private Long vendedorId;
    private long cantidadEntregas;
    private BigDecimal montoTotal;
}
