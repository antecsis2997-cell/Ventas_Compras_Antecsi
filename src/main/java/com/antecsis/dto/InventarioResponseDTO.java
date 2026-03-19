package com.antecsis.dto;

import java.math.BigDecimal;

public record InventarioResponseDTO(
    Long productoId,
    String codigo,
    String nombre,
    String descripcion,
    BigDecimal precio,
    String moneda,
    Integer stock,
    String unidadMedida,
    Integer stockMinimoAlerta,
    Long sectorId,
    String sectorNombre,
    String imagenUrl,
    Boolean activo,
    String estado
) {}
