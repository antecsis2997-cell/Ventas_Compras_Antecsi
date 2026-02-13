package com.antecsis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * LOCALIZACION según documento: Editar, Eliminar, Agregar.
 * DescripcionLocal, Imagenes (jpg, png, webp).
 */
@Entity
@Table(name = "localizaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Localizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "descripcion_local", length = 255)
    private String descripcionLocal;

    /** Ruta o URL de imagen (jpg, png, webp). */
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;
}
