package com.antecsis.dto.producto;

public record ProductoMasVendidoDTO(
    Long productoId,
    String nombre,
    Long cantidadVendida
) {}
