package com.antecsis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "receta_detalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecetaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id", nullable = false)
    private Receta receta;

    /** Insumo a consumir (debe tener esInsumo=true). */
    @ManyToOne(optional = false)
    @JoinColumn(name = "insumo_id")
    private Producto insumo;

    /** Cantidad base consumida por cada cantidadSalidaBase. */
    @Column(name = "cantidad_insumo_base", nullable = false)
    private Integer cantidadInsumoBase;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecetaDetalle that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }
}

