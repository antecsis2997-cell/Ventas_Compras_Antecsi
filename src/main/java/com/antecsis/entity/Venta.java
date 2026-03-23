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

    /** Total sin aplicar promociones/descuentos (para reglas basadas en umbrales). */
    @Column(name = "total_bruto", precision = 12, scale = 2)
    private BigDecimal totalBruto;

    /** Monto descontado por la promoción de visitas (10% cada vez que el cliente llega al múltiplo de 10). */
    @Column(name = "descuento_promocion_visitas_monto", precision = 12, scale = 2)
    private BigDecimal descuentoPromocionVisitasMonto;

    /** Porcentaje aplicado en la promoción de visitas (ej. 10.00). */
    @Column(name = "descuento_promocion_visitas_porcentaje", precision = 5, scale = 2)
    private BigDecimal descuentoPromocionVisitasPorcentaje;

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

    // ── Campos SEE del Contribuyente (SUNAT Facturación Electrónica) ──────

    @Enumerated(EnumType.STRING)
    @Column(name = "sunat_estado_cdr", length = 20)
    private SunatEstadoCdr sunatEstadoCdr;

    /** Código de respuesta del CDR SUNAT: "0"=aceptado, "2xxx"-"3xxx"=rechazado */
    @Column(name = "sunat_codigo_respuesta", length = 10)
    private String sunatCodigoRespuesta;

    /** Descripción del CDR (motivo de rechazo u observación) */
    @Column(name = "sunat_descripcion_cdr", columnDefinition = "TEXT")
    private String sunatDescripcionCdr;

    /** Fecha y hora en que se envió el comprobante a SUNAT */
    @Column(name = "sunat_fecha_envio")
    private java.time.LocalDateTime sunatFechaEnvio;

    /** Ticket SUNAT para boletas (proceso asíncrono via sendSummary) */
    @Column(name = "sunat_ticket", length = 50)
    private String sunatTicket;

    /** Hash SHA-256 del XML firmado (para verificación de integridad) */
    @Column(name = "sunat_hash", length = 255)
    private String sunatHash;

    /** Nombre del archivo XML enviado a SUNAT. Ej: "20123456789-01-F001-1" */
    @Column(name = "sunat_nombre_archivo", length = 100)
    private String sunatNombreArchivo;

    /** Número de intentos de envío a SUNAT (para reintentos) */
    @Column(name = "sunat_intentos", nullable = false, columnDefinition = "integer default 0")
    private int sunatIntentos = 0;
}
