package com.antecsis.entity;

/**
 * Estado del comprobante electrónico enviado a SUNAT.
 * <p>
 * PENDIENTE      → aún no enviado (boletas esperan resumen diario, o envío falló y se reintentará).
 * ACEPTADO       → SUNAT devolvió CDR con ResponseCode = 0.
 * OBSERVADO      → SUNAT aceptó pero con observaciones (advertencias no bloqueantes).
 * RECHAZADO      → SUNAT rechazó el comprobante (código 2000-3999). Debe corregirse.
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
