package com.antecsis.dto.venta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VentaRequestDTO {
    @NotNull(message = "Cliente es obligatorio")
    private Long clienteId;

    private Long metodoPagoId;

    private String observaciones;

    @NotEmpty(message = "Debe incluir al menos un item")
    private List<@Valid VentaItemDTO> items;
}
