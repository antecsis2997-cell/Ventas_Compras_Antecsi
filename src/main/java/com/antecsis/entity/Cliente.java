package com.antecsis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String telefono;

    private String tipoDocumento;

    private String documento;

    private String direccion;

    /** Zona de ventas: distrito del cliente (para métricas de logística). */
    private String distrito;

    /** Zona de ventas: provincia del cliente (para métricas de logística). */
    private String provincia;

    /** Zona de ventas: país del cliente (para métricas de logística). */
    private String pais;

    @ManyToOne
    @JoinColumn(name = "sector_id")
    private Sector sector;

    private Boolean activo = true;
}
