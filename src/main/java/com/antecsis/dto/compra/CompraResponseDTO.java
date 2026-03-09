package com.antecsis.dto.compra;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CompraResponseDTO(
    Long id,
    Long proveedorId,
    String proveedorNombre,
    String usuarioNombre,
    Long sectorId,
    String sectorNombre,
    String metodoPagoNombre,
    LocalDateTime fecha,
    BigDecimal total,
    String estado,
    String observaciones,
    String numeroDocumento,
    List<CompraItemResponseDTO> items
) {
    public record CompraItemResponseDTO(
        String productoNombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
    ) {}
}
