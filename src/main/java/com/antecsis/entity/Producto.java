package com.antecsis.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String codigo;

	@Column(nullable = false, length = 50)
	private String nombre;

	@Column(length = 50)
	private String descripcion;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal precio;

	@Column(precision = 12, scale = 2)
	private BigDecimal precioCompra;

	@Column(nullable = false)
	private Integer stock;

	@ManyToOne
	@JoinColumn(name = "categoria_id")
	private Categoria categoria;

	/** Unidad del producto: UND, KG, MG, GRAMOS (documento INSUMOS). */
	@Column(name = "unidad_medida", length = 20)
	private String unidadMedida;

	/** Ruta o URL de imagen (PNG, JPG). */
	@Column(name = "imagen_url", length = 500)
	private String imagenUrl;

	/** Umbral para alerta de stock bajo (pop-up). */
	@Column(name = "stock_minimo_alerta")
	private Integer stockMinimoAlerta;

	private Boolean activo = true;
}
