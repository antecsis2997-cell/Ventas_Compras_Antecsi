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
 * Sector/Sede según documento: Id_Sector, Nombre_Sector, Telefono, Direccion.
 * Usado para Cajero (Sede de Cajero) y Ventas (Sede de Venta).
 */
@Entity
@Table(name = "sectores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_sector", nullable = false, length = 100)
    private String nombreSector;

    @Column(length = 20)
    private String telefono;

    @Column(length = 200)
    private String direccion;

    /**
     * Prefijo/serie para boletas de este sector (ej. B137). Si está configurado, el número se genera automáticamente (B137-00000001).
     */
    @Column(name = "prefijo_boleta", length = 20)
    private String prefijoBoleta;

    /**
     * Prefijo/serie para facturas de este sector (ej. F137). Si está configurado, el número se genera automáticamente.
     */
    @Column(name = "prefijo_factura", length = 20)
    private String prefijoFactura;
}
