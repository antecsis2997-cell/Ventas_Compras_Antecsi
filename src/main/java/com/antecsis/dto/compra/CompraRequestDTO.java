package com.antecsis.dto.compra;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CompraRequestDTO {
    @NotNull(message = "Proveedor es obligatorio")
    private Long proveedorId;

    private Long metodoPagoId;

    private String observaciones;

    private String numeroDocumento;

    @NotEmpty(message = "Debe incluir al menos un item")
    private List<@Valid CompraItemDTO> items;
}
