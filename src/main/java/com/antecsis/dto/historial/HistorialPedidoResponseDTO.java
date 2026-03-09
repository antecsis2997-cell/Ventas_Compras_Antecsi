package com.antecsis.dto.historial;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistorialPedidoResponseDTO(
    Long id,
    Long ventaId,
    Long productoId,
    String nombreProducto,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal,
    LocalDateTime fecha
) {}
