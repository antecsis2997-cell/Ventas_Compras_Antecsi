package com.antecsis.dto.venta;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VentaRequestDTO {
    @NotNull
    private Long clienteId;

    @NotEmpty
    private List<VentaItemDTO> items;
}
