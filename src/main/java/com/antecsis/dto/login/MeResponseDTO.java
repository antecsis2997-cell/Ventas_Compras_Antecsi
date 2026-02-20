package com.antecsis.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Usuario actual (autenticado)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeResponseDTO {
    @Schema(description = "Nombre de usuario (login)")
    private String username;
    @Schema(description = "Nombre del rol: SUPERUSUARIO, ADMIN, CAJERO, etc.")
    private String rolNombre;
    @Schema(description = "ID de la sede asignada (null si no tiene)")
    private Long sedeId;
    @Schema(description = "Nombre de la sede")
    private String sedeNombre;
}
