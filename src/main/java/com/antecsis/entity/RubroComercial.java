package com.antecsis.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rubros_comerciales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RubroComercial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false)
    private int orden;
}
