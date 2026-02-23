package com.antecsis.dto.solicitudstock;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SolicitudStockRequestDTO {
    @NotBlank(message = "Asunto es obligatorio")
    private String asunto;
    @NotBlank(message = "Correo del remitente es obligatorio")
    private String remitenteEmail;
    private String nombreRemitente;
    @NotNull(message = "Producto es obligatorio")
    private Long productoId;
    private String unidadMedida; // UND, KILOS, GR
    @NotNull(message = "Cantidad es obligatoria")
    private Integer cantidad;
}
