package com.antecsis.dto.producto;

import java.math.BigDecimal;

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

    private String codigo;

    @NotBlank(message = "Nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "Precio de venta es obligatorio")
    @Positive(message = "Precio de venta debe ser mayor a 0")
    private BigDecimal precio;

    @Positive(message = "Precio de compra debe ser mayor a 0")
    private BigDecimal precioCompra;

    @NotNull(message = "Stock es obligatorio")
    @Min(value = 0, message = "Stock no puede ser negativo")
    private Integer stock;

    private Long categoriaId;
}
