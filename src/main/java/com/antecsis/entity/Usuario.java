package com.antecsis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class Usuario {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String nombre;
    private String apellido;
    private String correo;
    private Integer edad;
    private String cargo;  // Cargo del personal (documento: Ingresar Datos Usuario)

    @ManyToOne
    @JoinColumn(name = "sede_id")
    private Sector sede;  // Sede de Cajero / Sede de Venta (documento)

    /** Usuario principal que creó este usuario (para reglas de licencias: max 3 Cajeros, 1 Ventas). */
    @Column(name = "usuario_principal_id")
    private Long usuarioPrincipalId;

    @ManyToOne
    @JoinColumn(name = "rol_id")
    private Rol rol;

    private Boolean activo = true;
}
