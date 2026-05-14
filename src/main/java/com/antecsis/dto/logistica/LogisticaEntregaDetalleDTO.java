package com.antecsis.dto.logistica;

import java.time.LocalDateTime;

import java.math.BigDecimal;

/**
 * Detalle de entregas (delivery) para Logística:
 * vendedor, cliente, producto, cantidad y zona (distrito/provincia/departamento/país).
 */
public record LogisticaEntregaDetalleDTO(
    Long ventaId,
    LocalDateTime fechaVenta,
    Long vendedorId,
    String vendedorNombre,
    Long clienteId,
    String clienteNombre,
    String distrito,
    String provincia,
    String departamento,
    String pais,
    String productoNombre,
    Integer cantidad,
    BigDecimal subtotal
) {}

