package com.antecsis.dto.compra;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CompraResponseDTO {
    private Long id;
    private Long proveedorId;
    private String proveedorNombre;
    private String usuarioNombre;
    private Long sectorId;
    private String sectorNombre;
    private String metodoPagoNombre;
    private LocalDateTime fecha;
    private BigDecimal total;
    private String estado;
    private String observaciones;
    private String numeroDocumento;
    private List<CompraItemDTO> items;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CompraItemDTO {
        private String productoNombre;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }
}
