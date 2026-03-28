package com.antecsis.service;

import com.antecsis.entity.Sector;
import com.antecsis.entity.TipoDocumentoVenta;

/**
 * Genera el siguiente número de comprobante (serie + correlativo) por sector y tipo.
 * La serie proviene exclusivamente de ConfiguracionFiscal (SEE del Contribuyente).
 * Formato: {serie}-{00000001} con 8 dígitos de correlativo.
 */
public interface SecuenciaComprobanteService {

    /**
     * Genera el siguiente número usando la serie proporcionada (de ConfiguracionFiscal SUNAT).
     * Incrementa el contador de secuencia del sector.
     * @return número en formato "B001-00000001", o null si el sector o prefijo son nulos
     */
    String siguienteNumeroConPrefijo(Sector sector, TipoDocumentoVenta tipo, String prefijo);

    /**
     * Vista previa del siguiente número (solo lectura, no incrementa la secuencia).
     * Para mostrar en pantalla antes de registrar la venta.
     */
    String siguienteNumeroPreviewConPrefijo(Sector sector, TipoDocumentoVenta tipo, String prefijo);
}
