package com.antecsis.dto.suscripcion;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Suscripción con datos para listado")
public record SuscripcionResponseDTO(
    Long id,
    String nombreCliente,
    String ruc,
    Long sectorId,
    String sucursalNombre,
    String descripcion,
    String estado,
    LocalDate fechaCaducidad,
    String paquete,
    String correoReceptor,
    String correoAdmin,
    String rubroNombre,
    Boolean licenciaActivada,
    LocalDateTime fechaUltimaAlerta,
    String textoAlerta
) {}
