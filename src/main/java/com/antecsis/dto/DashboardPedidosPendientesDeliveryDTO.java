package com.antecsis.dto;

/**
 * Informe de órdenes con delivery que aún no se han entregado,
 * agrupado por estado de entrega (PENDIENTE / EN_CAMINO).
 */
public record DashboardPedidosPendientesDeliveryDTO(
        long totalRequiereDelivery,
        long pendientes,
        long enCamino,
        long entregados
) {}

