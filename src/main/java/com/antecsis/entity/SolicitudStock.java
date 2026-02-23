package com.antecsis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Solicitud de adquisición de stock. Cajero solicita, Logística aprueba/desaprueba.
 */
@Entity
@Table(name = "solicitudes_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario que crea la solicitud (Cajero). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellidos", nullable = false, length = 150)
    private String apellidos;

    @Column(name = "cargo", length = 50)
    private String cargo = "CAJERO";

    @Column(name = "asunto", nullable = false, length = 255)
    private String asunto;

    @Column(name = "remitente_email", nullable = false, length = 150)
    private String remitenteEmail;

    @Column(name = "nombre_remitente", length = 150)
    private String nombreRemitente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(name = "unidad_medida", length = 20)
    private String unidadMedida;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitudStock estado = EstadoSolicitudStock.PENDIENTE;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
