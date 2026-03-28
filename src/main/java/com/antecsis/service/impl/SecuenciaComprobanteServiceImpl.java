package com.antecsis.service.impl;

import com.antecsis.entity.Sector;
import com.antecsis.entity.SecuenciaComprobante;
import com.antecsis.entity.TipoDocumentoVenta;
import com.antecsis.repository.SecuenciaComprobanteRepository;
import com.antecsis.service.SecuenciaComprobanteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SecuenciaComprobanteServiceImpl implements SecuenciaComprobanteService {

    private static final int DIGITOS_CORRELATIVO = 8;

    private final SecuenciaComprobanteRepository repository;

    /**
     * Genera el siguiente número usando el prefijo proporcionado (serie de ConfiguracionFiscal SUNAT),
     * en lugar del prefijo almacenado en el Sector. Incrementa el mismo contador de secuencia.
     */
    @Override
    @Transactional
    public String siguienteNumeroConPrefijo(Sector sector, TipoDocumentoVenta tipo, String prefijo) {
        if (sector == null || sector.getId() == null || prefijo == null || prefijo.isBlank()) return null;
        String pref = prefijo.trim();

        long siguiente = 1;
        var seq = repository.findBySectorIdAndTipoDocumentoForUpdate(sector.getId(), tipo);
        if (seq.isPresent()) {
            siguiente = seq.get().getUltimoCorrelativo() + 1;
            seq.get().setUltimoCorrelativo(siguiente);
            repository.save(seq.get());
        } else {
            SecuenciaComprobante nueva = new SecuenciaComprobante();
            nueva.setSector(sector);
            nueva.setTipoDocumento(tipo);
            nueva.setUltimoCorrelativo(1);
            repository.save(nueva);
        }

        String correlativo = String.format("%0" + DIGITOS_CORRELATIVO + "d", siguiente);
        return pref + "-" + correlativo;
    }

    @Override
    @Transactional(readOnly = true)
    public String siguienteNumeroPreviewConPrefijo(Sector sector, TipoDocumentoVenta tipo, String prefijo) {
        if (sector == null || sector.getId() == null || prefijo == null || prefijo.isBlank()) return null;
        long siguiente = 1;
        var seq = repository.findBySectorIdAndTipoDocumento(sector.getId(), tipo);
        if (seq.isPresent()) {
            siguiente = seq.get().getUltimoCorrelativo() + 1;
        }
        String correlativo = String.format("%0" + DIGITOS_CORRELATIVO + "d", siguiente);
        return prefijo.trim() + "-" + correlativo;
    }
}
