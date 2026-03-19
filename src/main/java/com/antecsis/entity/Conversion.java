package com.antecsis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversiones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Conversion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receta_id")
    private Receta receta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "cantidad_producir", nullable = false)
    private Integer cantidadProducir;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoConversion estado;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
}

