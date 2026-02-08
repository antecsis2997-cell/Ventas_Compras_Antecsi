package com.antecsis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CompraRequestDTO {
    @NotBlank
    private String proveedor;

    @NotEmpty
    private List<CompraItemDTO> items;
}
