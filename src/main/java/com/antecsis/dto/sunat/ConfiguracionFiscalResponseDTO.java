package com.antecsis.dto.sunat;

/**
 * Datos de configuración fiscal devueltos al frontend.
 * Las credenciales SOL y el PFX NO se devuelven (solo se indica si están configurados).
 */
public record ConfiguracionFiscalResponseDTO(
    Long id,
    Long sectorId,
    String sectorNombre,
    String ruc,
    String razonSocial,
    String nombreComercial,
    String domicilioFiscal,
    String ubigeo,
    String distrito,
    String provincia,
    String departamento,
    boolean solConfigurado,        // true si las credenciales SOL están guardadas
    boolean certificadoConfigurado,// true si el PFX está cargado
    String serieBoleta,
    String serieFactura,
    String ambiente,
    boolean activo
) {}
