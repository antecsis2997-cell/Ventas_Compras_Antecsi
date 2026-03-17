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

    @Override
    @Transactional
    public String siguienteNumero(Sector sector, TipoDocumentoVenta tipo) {
        if (sector == null || sector.getId() == null) return null;
        String prefijo = tipo == TipoDocumentoVenta.BOLETA
                ? (sector.getPrefijoBoleta() != null && !sector.getPrefijoBoleta().isBlank() ? sector.getPrefijoBoleta().trim() : null)
                : (sector.getPrefijoFactura() != null && !sector.getPrefijoFactura().isBlank() ? sector.getPrefijoFactura().trim() : null);
        if (prefijo == null) return null;

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
        return prefijo + "-" + correlativo;
    }

    @Override
    @Transactional(readOnly = true)
    public String siguienteNumeroPreview(Sector sector, TipoDocumentoVenta tipo) {
        if (sector == null || sector.getId() == null) return null;
        String prefijo = tipo == TipoDocumentoVenta.BOLETA
                ? (sector.getPrefijoBoleta() != null && !sector.getPrefijoBoleta().isBlank() ? sector.getPrefijoBoleta().trim() : null)
                : (sector.getPrefijoFactura() != null && !sector.getPrefijoFactura().isBlank() ? sector.getPrefijoFactura().trim() : null);
        if (prefijo == null) return null;
        long siguiente = 1;
        var seq = repository.findBySectorIdAndTipoDocumento(sector.getId(), tipo);
        if (seq.isPresent()) {
            siguiente = seq.get().getUltimoCorrelativo() + 1;
        }
        String correlativo = String.format("%0" + DIGITOS_CORRELATIVO + "d", siguiente);
        return prefijo + "-" + correlativo;
    }
}
