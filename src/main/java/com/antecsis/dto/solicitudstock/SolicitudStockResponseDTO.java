package com.antecsis.dto.solicitudstock;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudStockResponseDTO {
    private Long id;
    private String nombre;
    private String apellidos;
    private String cargo;
    private String asunto;
    private String remitenteEmail;
    private String nombreRemitente;
    private Long productoId;
    private String productoNombre;
    private String unidadMedida;
    private Integer cantidad;
    private String estado;
    private LocalDateTime fechaCreacion;
}
