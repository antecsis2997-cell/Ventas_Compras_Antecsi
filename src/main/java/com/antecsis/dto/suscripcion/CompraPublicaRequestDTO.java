package com.antecsis.dto.suscripcion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Compra pública de plan (obtener el programa)")
public record CompraPublicaRequestDTO(
    @NotBlank(message = "Plan es obligatorio")
    @Schema(description = "BASICO, INTERMEDIO o AVANZADO")
    String plan,

    @NotBlank(message = "RUC es obligatorio")
    String ruc,

    @NotBlank(message = "Nombre del cliente/RUC es obligatorio")
    String nombreCliente,

    String nombreTitularTarjeta,
    String numeroTarjeta,
    String fechaCaducidadTarjeta,

    @Schema(description = "ID de sucursal (opcional)")
    Long sectorId
) {}
