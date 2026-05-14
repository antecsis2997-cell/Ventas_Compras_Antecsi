package com.antecsis.entity;

/**
 * Estado del comprobante electrónico enviado a SUNAT.
 * <p>
 * PENDIENTE      → aún no enviado (boletas esperan resumen diario, o envío falló y se reintentará).
 * ACEPTADO       → SUNAT devolvió CDR con {@code cbc:ResponseCode} igual a {@code "0"} (recepción aceptada).
 * OBSERVADO      → No aplica según la constancia SUNAT solo con ResponseCode de recepción; valor legado si existía en BD.
 * RECHAZADO      → SUNAT rechazó (ResponseCode ≠ 0; código de rechazo correspondiente).
 * ERROR_ENVIO    → error de comunicación con SUNAT (timeout, SOAP error). Se reintentará.
 * NO_APLICA      → venta sin comprobante electrónico (sin tipo de documento o sin configuración fiscal).
 */
public enum SunatEstadoCdr {
    PENDIENTE,
    ACEPTADO,
    OBSERVADO,
    RECHAZADO,
    ERROR_ENVIO,
    NO_APLICA
}
