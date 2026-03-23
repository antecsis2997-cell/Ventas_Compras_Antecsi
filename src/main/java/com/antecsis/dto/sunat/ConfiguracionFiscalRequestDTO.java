package com.antecsis.dto.sunat;

import jakarta.validation.constraints.*;

/**
 * Datos para crear o actualizar la configuración fiscal de una bodega.
 * Las credenciales SOL se envían en texto plano por HTTPS y se cifran antes de guardar.
 */
public record ConfiguracionFiscalRequestDTO(

    @NotNull Long sectorId,

    // Datos empresa
    @NotBlank @Size(min = 11, max = 11) String ruc,
    @NotBlank @Size(max = 250) String razonSocial,
    @Size(max = 250) String nombreComercial,
    @Size(max = 500) String domicilioFiscal,
    @Size(min = 6, max = 6) String ubigeo,
    @Size(max = 100) String distrito,
    @Size(max = 100) String provincia,
    @Size(max = 100) String departamento,

    // Credenciales SOL (en texto plano, se cifran al guardar)
    @NotBlank String solUsuario,   // Ej: "20123456789MODDATOS"
    @NotBlank String solClave,     // Clave SOL del usuario secundario

    // Certificado PFX en Base64 (opcional en actualización parcial)
    String certificadoPfxBase64,   // Contenido del archivo .PFX en Base64
    String certificadoClave,       // Contraseña del .PFX

    // Series
    @NotBlank @Pattern(regexp = "^[Bb]\\d{3}$") String serieBoleta,
    @NotBlank @Pattern(regexp = "^[Ff]\\d{3}$") String serieFactura,

    // Entorno
    @NotBlank @Pattern(regexp = "^(beta|produccion)$") String ambiente,
    boolean activo
) {}
