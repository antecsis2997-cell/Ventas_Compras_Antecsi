package com.antecsis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recetas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sector_id")
    private Sector sector;

    /** Producto vendible que se producirá (no es insumo). */
    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_salida_id")
    private Producto productoSalida;

    /** Cantidad base producida por receta. La conversión escala proporcionalmente. */
    @Column(name = "cantidad_salida_base", nullable = false)
    private Integer cantidadSalidaBase;

    @Column(nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RecetaDetalle> detalles = new ArrayList<>();
}

