package com.antecsis.dto.venta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VentaResponseDTO {
    private Long id;
    private Long clienteId;
    private String clienteNombre;
    private String usuarioNombre;
    private Long sectorId;
    private String sectorNombre;
    private String metodoPagoNombre;
    private LocalDateTime fecha;
    private BigDecimal total;
    private String estado;
    private String tipoDocumento;
    private String numeroDocumento;
    private String observaciones;
    private String moneda;
    private Boolean conCuotas;
    private Boolean requiereDelivery;
    private String tipoEntrega;
    private String direccionEntrega;
    private String estadoEntrega;
    private String entregadoPorNombre;
    private String codigoTracking;
    private String confirmacionCorreo;
    private String confirmacionTelefono;
    private LocalDateTime confirmacionFecha;
    private List<VentaItemDTO> items;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class VentaItemDTO {
        private String productoNombre;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }
}
