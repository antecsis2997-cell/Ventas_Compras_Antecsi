package com.antecsis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DashboardVentasDTO {
    private Long totalVentas;
    private Double montoTotal;
}
