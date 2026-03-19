package com.antecsis.dto.producto;

import java.math.BigDecimal;

public record ProductoResponseDTO(
    Long id,
    String codigo,
    String nombre,
    String descripcion,
    BigDecimal precio,
    BigDecimal precioCompra,
    Integer stock,
    Long categoriaId,
    String categoriaNombre,
    String moneda,
    String unidadMedida,
    String imagenUrl,
    Integer stockMinimoAlerta,
    String tipo,
    String marca,
    BigDecimal cantidad,
    Boolean activo,
    Boolean esInsumo
) {}
