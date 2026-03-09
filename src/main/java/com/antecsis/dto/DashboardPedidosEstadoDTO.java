package com.antecsis.dto;

/**
 * Dashboard Administración: Pedidos facturados (COMPLETADA) y anulados (ANULADA).
 */
public record DashboardPedidosEstadoDTO(
    long pedidosFacturados,
    long pedidosAnulados
) {}
