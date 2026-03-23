package com.antecsis.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Configuración fiscal (SUNAT - SEE del Contribuyente) por bodega/sector.
 * <p>
 * Las credenciales SOL y el contenido del certificado PFX se almacenan cifrados con AES-256.
 * La clave de cifrado se define en {@code sunat.cifrado.clave} (variable de entorno SUNAT_CIFRADO_CLAVE).
 */
@Entity
@Table(name = "configuracion_fiscal", uniqueConstraints = {
    @UniqueConstraint(columnNames = "sector_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionFiscal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    // ── Datos de la empresa ──────────────────────────────────────────────
    @Column(length = 11, nullable = false)
    private String ruc;

    @Column(name = "razon_social", length = 250, nullable = false)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 250)
    private String nombreComercial;

    @Column(name = "domicilio_fiscal", length = 500)
    private String domicilioFiscal;

    /** Código SUNAT de 6 dígitos. Ej: "150101" = Lima, Lima, Lima */
    @Column(length = 6)
    private String ubigeo;

    @Column(length = 100)
    private String distrito;

    @Column(length = 100)
    private String provincia;

    @Column(length = 100)
    private String departamento;

    // ── Credenciales SUNAT SOL (cifradas con AES-256) ────────────────────
    /** "{RUC}{usuarioSecundario}" cifrado. Ej: "20123456789MODDATOS" cifrado */
    @Column(name = "sol_usuario_cifrado", length = 500)
    private String solUsuarioCifrado;

    @Column(name = "sol_clave_cifrada", length = 500)
    private String solClaveCifrada;

    // ── Certificado digital PFX (cifrado) ────────────────────────────────
    /** Contenido binario del .PFX codificado en Base64, cifrado con AES-256 */
    @Column(name = "certificado_pfx_cifrado", columnDefinition = "TEXT")
    private String certificadoPfxCifrado;

    /** Contraseña del archivo .PFX, cifrada con AES-256 */
    @Column(name = "certificado_clave_cifrada", length = 500)
    private String certificadoClaveCifrada;

    // ── Series ────────────────────────────────────────────────────────────
    /** Serie para boletas. Debe comenzar con "B" (4 caracteres). Ej: "B001" */
    @Column(name = "serie_boleta", length = 4)
    private String serieBoleta;

    /** Serie para facturas. Debe comenzar con "F" (4 caracteres). Ej: "F001" */
    @Column(name = "serie_factura", length = 4)
    private String serieFactura;

    // ── Entorno ───────────────────────────────────────────────────────────
    /** "beta" (pruebas SUNAT) o "produccion" */
    @Column(length = 15, nullable = false)
    private String ambiente;

    /** true = configuración activa para enviar a SUNAT */
    @Column(nullable = false)
    private boolean activo = false;
}
