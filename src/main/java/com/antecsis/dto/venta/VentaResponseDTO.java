package com.antecsis.dto.venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaResponseDTO(
    Long id,
    Long clienteId,
    String clienteNombre,
    String usuarioNombre,
    Long sectorId,
    String sectorNombre,
    String metodoPagoNombre,
    LocalDateTime fecha,
    BigDecimal total,
    String estado,
    String tipoDocumento,
    String numeroDocumento,
    String observaciones,
    String moneda,
    Boolean conCuotas,
    Boolean requiereDelivery,
    String tipoEntrega,
    String direccionEntrega,
    String estadoEntrega,
    String entregadoPorNombre,
    String codigoTracking,
    String confirmacionCorreo,
    String confirmacionTelefono,
    LocalDateTime confirmacionFecha,
    List<VentaItemResponseDTO> items
) {
    public record VentaItemResponseDTO(
        String productoNombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
    ) {}
}
