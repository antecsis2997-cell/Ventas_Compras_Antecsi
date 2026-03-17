package com.antecsis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Correlativo de comprobantes por sector y tipo (BOLETA/FACTURA).
 * Una fila por cada combinación sector + tipo; ultimoCorrelativo se incrementa en cada venta.
 */
@Entity
@Table(name = "secuencia_comprobante", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "sector_id", "tipo_documento" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SecuenciaComprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 20)
    private TipoDocumentoVenta tipoDocumento;

    @Column(name = "ultimo_correlativo", nullable = false)
    private long ultimoCorrelativo = 0;
}
