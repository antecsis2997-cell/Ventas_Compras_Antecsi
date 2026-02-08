package com.antecsis.dto.producto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequestDTO {
	@NotBlank(message = "Nombre es obligatorio")
    private String nombre;

    @NotNull
    @Positive(message = "Precio debe ser mayor a 0")
    private Double precio;

    @NotNull
    @Min(value = 0, message = "Stock no puede ser negativo")
    private Integer stock;
}
