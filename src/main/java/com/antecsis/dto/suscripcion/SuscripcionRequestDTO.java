package com.antecsis.dto.suscripcion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Datos para crear/actualizar suscripción")
public record SuscripcionRequestDTO(
    @NotBlank(message = "Nombre del cliente es obligatorio")
    String nombreCliente,
    String ruc,
    @NotNull(message = "Sector es obligatorio")
    Long sectorId,
    String descripcion,
    @NotBlank(message = "Estado es obligatorio")
    String estado,
    @NotNull(message = "Fecha de caducidad es obligatoria")
    LocalDate fechaCaducidad,
    String paquete,
    String correoReceptor
) {}
