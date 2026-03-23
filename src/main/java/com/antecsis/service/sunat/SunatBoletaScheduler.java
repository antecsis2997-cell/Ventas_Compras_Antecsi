package com.antecsis.service.sunat;

import com.antecsis.entity.*;
import com.antecsis.repository.ConfiguracionFiscalRepository;
import com.antecsis.repository.VentaRepository;
import com.antecsis.service.CryptoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler que maneja el ciclo de vida de los comprobantes electrónicos de tipo BOLETA:
 * <p>
 * 1. Cada noche (cron configurable) envía el "Resumen Diario" con todas las boletas PENDIENTES del día.
 * 2. Cada 30 minutos consulta los tickets en espera para actualizar su estado CDR.
 * 3. Reintenta boletas/facturas en estado ERROR_ENVIO (máximo 3 intentos).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SunatBoletaScheduler {

    private final VentaRepository ventaRepo;
    private final ConfiguracionFiscalRepository configRepo;
    private final SunatXmlGeneratorService xmlGenerator;
    private final SunatFirmaService firmaService;
    private final SunatSoapService soapService;
    private final SunatVentaService sunatVentaService;
    private final CryptoService crypto;

    @Value("${sunat.resumen.cron:0 0 23 * * ?}")
    private String resumenCron;

    /** Envía el resumen diario de boletas a SUNAT. Cron configurable (por defecto 11 PM). */
    @Scheduled(cron = "${sunat.resumen.cron:0 0 23 * * ?}")
    @Transactional
    public void enviarResumenDiarioBoletas() {
        log.info("Iniciando envío de resumen diario de boletas SUNAT...");
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicio = hoy.atStartOfDay();
        LocalDateTime fin = hoy.atTime(23, 59, 59);

        List<ConfiguracionFiscal> configs = configRepo.findAll().stream()
                .filter(ConfiguracionFiscal::isActivo)
                .toList();

        for (ConfiguracionFiscal cfg : configs) {
            try {
                List<Venta> boletas = ventaRepo.findBoletasPendientesParaResumen(
                        cfg.getSector().getId(), SunatEstadoCdr.PENDIENTE, inicio, fin);

                if (boletas.isEmpty()) {
                    log.debug("Sin boletas pendientes para sector {}", cfg.getSector().getNombreSector());
                    continue;
                }

                log.info("Enviando resumen diario: {} boletas del sector {}",
                        boletas.size(), cfg.getSector().getNombreSector());

                // Convención SUNAT: {RUC}-RC-{YYYYMMDD}-{NRO_3_DIGITOS}
                // NRO = 1 por defecto (si se emiten múltiples resúmenes el mismo día usar secuencia)
                String nombreArchivo = xmlGenerator.calcularNombreArchivoResumen(cfg, hoy, 1);

                String xmlResumen = xmlGenerator.generarResumenDiario(boletas, cfg, hoy);
                String xmlFirmado = firmarSiHayCertificado(xmlResumen, cfg);

                String solUsuario = crypto.descifrar(cfg.getSolUsuarioCifrado());
                String solClave = crypto.descifrar(cfg.getSolClaveCifrada());

                SunatCdrResult resultado = soapService.sendSummary(
                        solUsuario, solClave, xmlFirmado, nombreArchivo, cfg.getAmbiente());

                for (Venta boleta : boletas) {
                    boleta.setSunatTicket(resultado.getTicket());
                    boleta.setSunatFechaEnvio(LocalDateTime.now());
                    boleta.setSunatNombreArchivo(nombreArchivo);
                    ventaRepo.save(boleta);
                }

                log.info("Resumen diario enviado. Ticket SUNAT: {}", resultado.getTicket());

            } catch (Exception e) {
                log.error("Error enviando resumen diario para sector {}: {}",
                        cfg.getSector().getNombreSector(), e.getMessage(), e);
            }
        }
    }

    /** Consulta los tickets pendientes de boletas enviadas en resumen diario. */
    @Scheduled(fixedDelay = 30 * 60 * 1000) // cada 30 minutos
    @Transactional
    public void consultarTicketsPendientes() {
        List<Venta> conTicket = ventaRepo.findBoletasConTicketPendiente(SunatEstadoCdr.PENDIENTE);
        if (conTicket.isEmpty()) return;

        log.info("Consultando {} tickets SUNAT pendientes...", conTicket.size());
        for (Venta venta : conTicket) {
            if (venta.getSunatTicket() == null) continue;
            Long sectorId = venta.getSector() != null ? venta.getSector().getId() : null;
            configRepo.findBySectorIdAndActivoTrue(sectorId).ifPresent(cfg ->
                    sunatVentaService.consultarTicket(venta, cfg));
        }
    }

    /** Reintenta ventas en ERROR_ENVIO (máximo 3 intentos). */
    @Scheduled(fixedDelay = 60 * 60 * 1000) // cada hora
    @Transactional
    public void reintentarErrores() {
        List<Venta> errores = ventaRepo.findVentasParaReintentar(SunatEstadoCdr.ERROR_ENVIO, 3);
        if (errores.isEmpty()) return;

        log.info("Reintentando {} ventas con error SUNAT...", errores.size());
        for (Venta venta : errores) {
            try {
                sunatVentaService.enviarComprobante(venta);
            } catch (Exception e) {
                log.error("Error reintentando venta #{}: {}", venta.getId(), e.getMessage());
            }
        }
    }

    private String firmarSiHayCertificado(String xml, ConfiguracionFiscal cfg) {
        if (cfg.getCertificadoPfxCifrado() == null) return xml;
        String pfxBase64 = crypto.descifrar(cfg.getCertificadoPfxCifrado());
        String pfxClave = crypto.descifrar(cfg.getCertificadoClaveCifrada());
        return firmaService.firmar(xml, pfxBase64, pfxClave);
    }
}
