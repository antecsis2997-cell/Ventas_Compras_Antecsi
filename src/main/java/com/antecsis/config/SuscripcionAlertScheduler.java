package com.antecsis.config;

import com.antecsis.service.SuscripcionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuscripcionAlertScheduler {

    private final SuscripcionService suscripcionService;

    @Scheduled(cron = "${app.suscripciones.alerta-cron:0 0 8 * * ?}")
    public void ejecutarAlertas() {
        log.debug("Ejecutando alertas automáticas de suscripciones vencidas");
        suscripcionService.ejecutarAlertasAutomaticas();
        log.debug("Ejecutando alertas de suscripciones próximas a vencer");
        suscripcionService.ejecutarAlertasProximoVencimiento();
    }
}
