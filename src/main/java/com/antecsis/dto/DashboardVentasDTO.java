package com.antecsis.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DashboardVentasDTO {
    private Long totalVentas;
    private BigDecimal montoTotal;
}
