package com.antecsis.dto.venta;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para confirmar la recepción del producto por el cliente")
public record ConfirmacionEntregaRequestDTO(
    @Schema(description = "Firma digital en base64 (opcional)")
    String firmaBase64,

    @Schema(description = "Correo del cliente")
    String correo,

    @Schema(description = "Teléfono del cliente")
    String telefono
) {}
