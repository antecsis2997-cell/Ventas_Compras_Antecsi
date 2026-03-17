package com.antecsis.service;

import com.antecsis.entity.Sector;
import com.antecsis.entity.TipoDocumentoVenta;

/**
 * Genera el siguiente número de comprobante (serie + correlativo) por sector y tipo.
 * Formato: prefijo-00000001 (8 dígitos, al estilo establecimiento + punto de emisión).
 */
public interface SecuenciaComprobanteService {

    /**
     * Obtiene el siguiente número para el sector y tipo. El sector debe tener configurado
     * prefijoBoleta (para BOLETA) o prefijoFactura (para FACTURA).
     * @return número en formato "B137-00000001" o null si el sector no tiene prefijo para ese tipo
     */
    String siguienteNumero(Sector sector, TipoDocumentoVenta tipo);

    /**
     * Vista previa del siguiente número (solo lectura, no incrementa la secuencia).
     * Para mostrar en pantalla antes de registrar la venta.
     */
    String siguienteNumeroPreview(Sector sector, TipoDocumentoVenta tipo);
}
