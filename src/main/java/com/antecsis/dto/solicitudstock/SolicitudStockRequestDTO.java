package com.antecsis.dto.solicitudstock;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitudStockRequestDTO(
    @NotBlank(message = "Asunto es obligatorio")
    String asunto,

    @NotBlank(message = "Correo del remitente es obligatorio")
    String remitenteEmail,

    String nombreRemitente,

    @NotNull(message = "Producto es obligatorio")
    Long productoId,

    String unidadMedida,

    @NotNull(message = "Cantidad es obligatoria")
    Integer cantidad
) {}
