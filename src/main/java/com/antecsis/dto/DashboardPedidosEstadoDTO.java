package com.antecsis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Dashboard Administración: Pedidos facturados (COMPLETADA) y anulados (ANULADA).
 */
@Getter
@Setter
@AllArgsConstructor
public class DashboardPedidosEstadoDTO {
    private long pedidosFacturados;
    private long pedidosAnulados;
}
