package com.antecsis.dto.compra;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CompraRequestDTO {
    @NotNull
    private Long proveedorId;

    @NotEmpty
    private List<CompraItemDTO> items;
}
