package com.antecsis.dto.producto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
    @Size(max = 50, message = "Título/nombre máximo 50 caracteres (INSUMOS)")
    private String nombre;

    @Size(max = 50, message = "Descripción máximo 50 caracteres (INSUMOS)")
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

    /** Unidad: UND, KG, MG, GRAMOS (documento INSUMOS). */
    private String unidadMedida;
    private String imagenUrl;
    private Integer stockMinimoAlerta;
}
