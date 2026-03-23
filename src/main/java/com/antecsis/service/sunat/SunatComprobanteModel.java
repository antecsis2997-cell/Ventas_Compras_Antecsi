package com.antecsis.service.sunat;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Modelo de datos pre-calculados que se pasan a las plantillas Freemarker
 * para generar el XML UBL 2.1 de comprobantes electrónicos SUNAT.
 */
@Getter
@Builder
public class SunatComprobanteModel {

    // ── Emisor ──────────────────────────────────────────────────────────
    private String emisorRuc;
    private String emisorRazonSocial;
    private String emisorNombreComercial;
    private String emisorDomicilio;
    private String emisorUbigeo;         // "150101"
    private String emisorDistrito;
    private String emisorProvincia;
    private String emisorDepartamento;

    // ── Comprobante ─────────────────────────────────────────────────────
    /** "01" = Factura, "03" = Boleta */
    private String tipoDocumento;
    /** "0101" para facturas/boletas con IGV (venta interna) */
    private String tipoDocumentoListId;
    private String serie;
    private int correlativo;
    private String fechaEmision;         // "yyyy-MM-dd"
    private String horaEmision;          // "HH:mm:ss"
    private String moneda;               // "PEN" o "USD"
    /** Ej: "SON: CIENTO DIECIOCHO CON 00/100 SOLES" */
    private String notaMontoLetras;

    // ── Receptor ────────────────────────────────────────────────────────
    /** Código catálogo 06: "0"=sin doc, "1"=DNI, "6"=RUC */
    private String receptorTipoDoc;
    private String receptorNumDoc;
    private String receptorNombre;
    private String receptorDireccion;

    // ── Totales ─────────────────────────────────────────────────────────
    /** Sumatoria de valores venta sin IGV (base imponible) */
    private BigDecimal totalValorVenta;
    /** Sumatoria de IGV de todas las líneas */
    private BigDecimal totalIgv;
    /** Monto total a pagar (con IGV) */
    private BigDecimal totalPagar;
    /** Forma de pago: "Contado" o "Credito" */
    private String formaPago;

    // ── Líneas ──────────────────────────────────────────────────────────
    private List<Linea> lineas;

    @Getter
    @Builder
    public static class Linea {
        private int numero;
        private String descripcion;
        private String codigoProducto;
        private String unidadMedida;       // "NIU" (unidades por defecto)
        private BigDecimal cantidad;
        /** Precio unitario SIN IGV */
        private BigDecimal valorUnitarioSinIgv;
        /** Precio unitario CON IGV (lo que paga el cliente) */
        private BigDecimal precioConIgv;
        /** Valor de venta de la línea SIN IGV (valorUnitarioSinIgv × cantidad) */
        private BigDecimal valorVentaLinea;
        /** IGV de la línea */
        private BigDecimal igvLinea;
        /** "10" = Gravado Oneroso (catálogo 07, el más común) */
        private String codigoAfectacionIgv;
    }
}
