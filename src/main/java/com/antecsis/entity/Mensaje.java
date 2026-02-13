package com.antecsis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CHAT: Message -> Nombre Receptor, ITEM, Descripción, Precio, Estado (Rojo Emergente, Amarillo Leve).
 */
@Entity
@Table(name = "mensajes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_receptor", nullable = false, length = 150)
    private String nombreReceptor;

    @Column(length = 200)
    private String item;

    @Column(length = 500)
    private String descripcion;

    @Column(precision = 12, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EstadoSolicitud estado;

    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "nombre_emisor", length = 150)
    private String nombreEmisor;
}
