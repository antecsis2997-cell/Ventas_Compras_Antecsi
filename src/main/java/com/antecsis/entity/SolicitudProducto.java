package com.antecsis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Logística: Solicitud de Producto. Emisor, ITEM, Descripción, Precio, Estado (Rojo Emergente, Amarillo Leve).
 */
@Entity
@Table(name = "solicitudes_producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_emisor", nullable = false, length = 150)
    private String nombreEmisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(length = 255)
    private String descripcion;

    @Column(precision = 12, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado = EstadoSolicitud.LEVE;

    private LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean atendida = false;
}
