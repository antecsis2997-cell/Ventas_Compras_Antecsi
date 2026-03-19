package com.antecsis.dto.producto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductoRequestDTO(
    String codigo,

    @NotBlank(message = "Nombre es obligatorio")
    @Size(max = 50, message = "Título/nombre máximo 50 caracteres (INSUMOS)")
    String nombre,

    @Size(max = 50, message = "Descripción máximo 50 caracteres (INSUMOS)")
    String descripcion,

    @NotNull(message = "Precio de venta es obligatorio")
    @Positive(message = "Precio de venta debe ser mayor a 0")
    BigDecimal precio,

    @Positive(message = "Precio de compra debe ser mayor a 0")
    BigDecimal precioCompra,

    @NotNull(message = "Stock es obligatorio")
    @Min(value = 0, message = "Stock no puede ser negativo")
    Integer stock,

    Long categoriaId,
    String moneda,
    String unidadMedida,
    String imagenUrl,
    Integer stockMinimoAlerta,
    String tipo,
    String marca,
    BigDecimal cantidad,
    /** true = insumo (materia prima). false = producto vendible. */
    Boolean esInsumo
) {}
