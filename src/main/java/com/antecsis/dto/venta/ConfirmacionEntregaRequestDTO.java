package com.antecsis.dto.venta;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para confirmar la recepción del producto por el cliente")
public class ConfirmacionEntregaRequestDTO {
    @Schema(description = "Firma digital en base64 (opcional)")
    private String firmaBase64;
    @Schema(description = "Correo del cliente")
    private String correo;
    @Schema(description = "Teléfono del cliente")
    private String telefono;
}
