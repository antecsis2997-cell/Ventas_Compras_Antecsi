package com.antecsis.service.sunat;

import com.antecsis.entity.*;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.VentaRepository;
import com.antecsis.service.ConfiguracionFiscalService;
import com.antecsis.service.CryptoService;
import com.antecsis.service.SecuenciaComprobanteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Orquesta el envío de comprobantes electrónicos a SUNAT para el SEE del Contribuyente.
 * <p>
 * - Para FACTURAS: genera XML → firma → sendBill (síncrono) → guarda CDR.
 * - Para BOLETAS: genera XML → actualiza venta como PENDIENTE (el scheduler la envía en resumen diario).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SunatVentaService {

    private final ConfiguracionFiscalService configService;
    private final SunatXmlGeneratorService xmlGenerator;
    private final SunatFirmaService firmaService;
    private final SunatSoapService soapService;
    private final CryptoService crypto;
    private final SecuenciaComprobanteService secuenciaService;
    private final VentaRepository ventaRepo;

    /**
     * Intenta enviar el comprobante a SUNAT.
     * Debe llamarse DESPUÉS de que la transacción que guardó la venta haya hecho commit
     * (via afterCommit hook), de modo que la venta ya sea visible en BD.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enviarComprobante(Long ventaId) {
        Venta venta = ventaRepo.findById(ventaId)
                .orElseThrow(() -> new BusinessException("Venta no encontrada: " + ventaId));

        if (venta.getTipoDocumento() == null) {
            actualizarEstado(venta, SunatEstadoCdr.NO_APLICA, null, "Sin tipo de documento", null);
            return;
        }

        Long sectorId = venta.getSector() != null ? venta.getSector().getId() : null;
        Optional<ConfiguracionFiscal> cfgOpt = configService.buscarActivaPorSector(sectorId);
        if (cfgOpt.isEmpty()) {
            actualizarEstado(venta, SunatEstadoCdr.NO_APLICA, null, "Sin configuración fiscal activa para el sector", null);
            return;
        }

        ConfiguracionFiscal cfg = cfgOpt.get();
        boolean esFactura = TipoDocumentoVenta.FACTURA == venta.getTipoDocumento();

        // Obtener correlativo usando la secuencia existente del sistema
        int correlativo = obtenerCorrelativo(venta, cfg);
        String nombreArchivo = xmlGenerator.calcularNombreArchivo(venta, cfg, correlativo);

        try {
            String xmlSinFirma = xmlGenerator.generarXml(venta, cfg, correlativo);
            String xmlFirmado = firmarSiHayCertificado(xmlSinFirma, cfg);

            venta.setSunatNombreArchivo(nombreArchivo);
            venta.setSunatIntentos(venta.getSunatIntentos() + 1);
            venta.setSunatFechaEnvio(LocalDateTime.now());

            if (esFactura) {
                // Envío síncrono
                String solUsuario = crypto.descifrar(cfg.getSolUsuarioCifrado());
                String solClave = crypto.descifrar(cfg.getSolClaveCifrada());
                SunatCdrResult cdr = soapService.sendBill(solUsuario, solClave, xmlFirmado, nombreArchivo, cfg.getAmbiente());
                procesarCdr(venta, cdr);
            } else {
                // Las boletas se envían en resumen diario (asíncrono) — marcar como PENDIENTE
                actualizarEstado(venta, SunatEstadoCdr.PENDIENTE, null, "Pendiente de resumen diario", null);
            }

            ventaRepo.save(venta);

        } catch (BusinessException e) {
            // Error de negocio (ej: cliente sin RUC para factura) — no reintentar automáticamente
            log.error("Error de validación enviando comprobante {} a SUNAT: {}", nombreArchivo, e.getMessage());
            actualizarEstado(venta, SunatEstadoCdr.RECHAZADO, "ERROR_VALIDACION", e.getMessage(), null);
            ventaRepo.save(venta);
        } catch (Exception e) {
            log.error("Error enviando comprobante {} a SUNAT: {}", nombreArchivo, e.getMessage(), e);
            actualizarEstado(venta, SunatEstadoCdr.ERROR_ENVIO, "ERROR", e.getMessage(), null);
            ventaRepo.save(venta);
        }
    }

    /**
     * Reintenta el envío de una venta que quedó en ERROR_ENVIO.
     * Para boletas en PENDIENTE, el scheduler del resumen diario las maneja.
     */
    @Transactional
    public void reintentarEnvio(Long ventaId) {
        Venta venta = ventaRepo.findById(ventaId)
                .orElseThrow(() -> new BusinessException("Venta no encontrada"));
        if (SunatEstadoCdr.ERROR_ENVIO != venta.getSunatEstadoCdr()) {
            throw new BusinessException("Solo se pueden reintentar ventas en estado ERROR_ENVIO");
        }
        enviarComprobante(ventaId);
    }

    /**
     * Consulta el ticket de una boleta (para el scheduler de resumen diario).
     */
    @Transactional
    public void consultarTicket(Venta venta, ConfiguracionFiscal cfg) {
        if (venta.getSunatTicket() == null) return;
        String solUsuario = crypto.descifrar(cfg.getSolUsuarioCifrado());
        String solClave = crypto.descifrar(cfg.getSolClaveCifrada());
        SunatCdrResult resultado = soapService.getStatus(solUsuario, solClave, venta.getSunatTicket(), cfg.getAmbiente());

        // 98 y 99 = "en proceso" según SUNAT; esperar próximo ciclo del scheduler
        if ("98".equals(resultado.getCodigoRespuesta()) || "99".equals(resultado.getCodigoRespuesta())) {
            return;
        }
        procesarCdr(venta, resultado);
        ventaRepo.save(venta);
    }

    // ─────────────────────────────────────────────────────────────────────

    private void procesarCdr(Venta venta, SunatCdrResult cdr) {
        if (cdr.getTicket() != null) {
            venta.setSunatTicket(cdr.getTicket());
            actualizarEstado(venta, SunatEstadoCdr.PENDIENTE, null, "Ticket recibido: " + cdr.getTicket(), cdr.getTicket());
        } else if (cdr.isAceptado()) {
            SunatEstadoCdr estado = cdr.isConObservaciones() ? SunatEstadoCdr.OBSERVADO : SunatEstadoCdr.ACEPTADO;
            actualizarEstado(venta, estado, cdr.getCodigoRespuesta(), cdr.getDescripcion(), null);
        } else if ("ERROR".equals(cdr.getCodigoRespuesta())) {
            actualizarEstado(venta, SunatEstadoCdr.ERROR_ENVIO, cdr.getCodigoRespuesta(), cdr.getDescripcion(), null);
        } else {
            actualizarEstado(venta, SunatEstadoCdr.RECHAZADO, cdr.getCodigoRespuesta(), cdr.getDescripcion(), null);
        }
    }

    private void actualizarEstado(Venta venta, SunatEstadoCdr estado, String codigo, String desc, String ticket) {
        venta.setSunatEstadoCdr(estado);
        venta.setSunatCodigoRespuesta(codigo);
        venta.setSunatDescripcionCdr(desc);
        if (ticket != null) venta.setSunatTicket(ticket);
        log.info("Venta #{} SUNAT estado={} codigo={} desc={}", venta.getId(), estado, codigo, desc);
    }

    private String firmarSiHayCertificado(String xmlSinFirma, ConfiguracionFiscal cfg) {
        if (cfg.getCertificadoPfxCifrado() == null || cfg.getCertificadoClaveCifrada() == null) {
            log.warn("Sector {} no tiene certificado PFX configurado. Enviando sin firma.", cfg.getSector().getId());
            return xmlSinFirma;
        }
        String pfxBase64 = crypto.descifrar(cfg.getCertificadoPfxCifrado());
        String pfxClave = crypto.descifrar(cfg.getCertificadoClaveCifrada());
        return firmaService.firmar(xmlSinFirma, pfxBase64, pfxClave);
    }

    private int obtenerCorrelativo(Venta venta, ConfiguracionFiscal cfg) {
        // El número de documento ya fue generado al crear la venta (SecuenciaComprobanteService)
        // Extraemos el correlativo del numeroDocumento si existe
        if (venta.getNumeroDocumento() != null && venta.getNumeroDocumento().contains("-")) {
            try {
                String[] partes = venta.getNumeroDocumento().split("-");
                return Integer.parseInt(partes[partes.length - 1]);
            } catch (NumberFormatException ignored) {}
        }
        // Fallback: usar el ID de la venta
        return venta.getId().intValue();
    }
}
