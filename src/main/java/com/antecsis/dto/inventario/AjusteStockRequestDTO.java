package com.antecsis.dto.inventario;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AjusteStockRequestDTO {

    @NotNull(message = "Producto es obligatorio")
    private Long productoId;

    @NotNull(message = "Nuevo stock es obligatorio")
    @Min(value = 0, message = "Stock no puede ser negativo")
    private Integer nuevoStock;

    @Size(max = 200, message = "Motivo máximo 200 caracteres")
    private String motivo;
}
