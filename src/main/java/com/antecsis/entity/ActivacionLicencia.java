package com.antecsis.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "activaciones_licencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivacionLicencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suscripcion_id", nullable = false, unique = true)
    private Suscripcion suscripcion;

    /** Debe coincidir con el claim jti del JWT de licencia. */
    @Column(nullable = false, unique = true, length = 80)
    private String jti;

    @Column(name = "vigencia_hasta", nullable = false)
    private LocalDate vigenciaHasta;

    @Column(nullable = false)
    private boolean activada;

    @Column(name = "fecha_activacion")
    private LocalDateTime fechaActivacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activada_por_usuario_id")
    private Usuario activadaPor;
}
