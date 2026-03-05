package com.antecsis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ventas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @ManyToOne
    @JoinColumn(name = "metodo_pago_id")
    private MetodoPago metodoPago;

    private LocalDateTime fecha;

    @Column(precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVenta estado = EstadoVenta.COMPLETADA;

    /** FACTURA o BOLETA (documento: modelo Factura y Boleta). */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento")
    private TipoDocumentoVenta tipoDocumento;

    /** Número de factura o boleta. */
    @Column(name = "numero_documento", length = 50)
    private String numeroDocumento;

    private String observaciones;

    @Column(length = 3, nullable = false)
    private String moneda = "PEN";

    /** Indica si el pago con tarjeta es con cuotas. */
    @Column(name = "con_cuotas")
    private Boolean conCuotas;

    /** Indica si la venta requiere delivery. Si true, venta inicia en PENDIENTE hasta que Logística marque entregado. */
    @Column(name = "requiere_delivery")
    private Boolean requiereDelivery = false;

    /** INMEDIATA o PROGRAMADA_3_5 (3 a 5 días). */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entrega", length = 20)
    private TipoEntrega tipoEntrega;

    /** Dirección de entrega cuando tipo es INMEDIATA. */
    @Column(name = "direccion_entrega", length = 500)
    private String direccionEntrega;

    /** Estado del delivery: PENDIENTE, EN_CAMINO, ENTREGADO. */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_entrega", length = 20)
    private EstadoEntrega estadoEntrega;

    /** Usuario que marcó la entrega como completada (logística). */
    @ManyToOne
    @JoinColumn(name = "usuario_entrega_id")
    private Usuario usuarioEntrega;

    /** Código de tracking para seguimiento del envío. */
    @Column(name = "codigo_tracking", length = 50)
    private String codigoTracking;

    /** Firma digital del cliente al confirmar recepción (base64). */
    @Column(name = "confirmacion_firma", columnDefinition = "TEXT")
    private String confirmacionFirma;

    /** Correo del cliente en confirmación. */
    @Column(name = "confirmacion_correo", length = 255)
    private String confirmacionCorreo;

    /** Teléfono del cliente en confirmación. */
    @Column(name = "confirmacion_telefono", length = 50)
    private String confirmacionTelefono;

    /** Fecha de confirmación del cliente. */
    @Column(name = "confirmacion_fecha")
    private java.time.LocalDateTime confirmacionFecha;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL)
    private List<VentaDetalle> detalles;
}
