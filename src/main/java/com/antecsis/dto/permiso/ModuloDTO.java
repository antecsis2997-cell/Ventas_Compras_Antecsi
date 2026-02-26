package com.antecsis.dto.permiso;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModuloDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String icono;
    private Integer orden;
    private boolean asignado;
}
