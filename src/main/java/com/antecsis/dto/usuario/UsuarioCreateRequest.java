package com.antecsis.dto.usuario;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class UsuarioCreateRequest {
	@NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String rol;  // ADMIN, CAJERO o ALMACENERO (solo SUPERUSUARIO puede crear)

    private String nombre;
    private String apellido;
    private String correo;
    private LocalDate fechaNacimiento;
    private Long sedeId;  // Sector (Sede de Cajero / Sede de Venta)
}
