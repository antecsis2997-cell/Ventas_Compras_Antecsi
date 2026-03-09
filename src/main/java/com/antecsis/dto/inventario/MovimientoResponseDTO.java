package com.antecsis.dto.inventario;

import java.time.LocalDateTime;

public record MovimientoResponseDTO(
    Long id,
    Long productoId,
    String productoNombre,
    String tipo,
    Integer cantidad,
    Integer stockAnterior,
    Integer stockNuevo,
    String motivo,
    Long referenciaId,
    String usuarioNombre,
    LocalDateTime fecha
) {}
