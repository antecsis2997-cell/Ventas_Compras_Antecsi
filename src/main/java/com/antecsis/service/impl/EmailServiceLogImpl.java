package com.antecsis.service.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.antecsis.service.EmailService;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementación que solo registra en log cuando no hay SMTP configurado.
 * Útil en desarrollo. Para producción, configure MAIL_HOST, MAIL_USERNAME, MAIL_PASSWORD.
 */
@Slf4j
@Service
@ConditionalOnMissingBean(EmailService.class)
public class EmailServiceLogImpl implements EmailService {

    @Override
    public void enviarRecuperacionContrasena(String para, String nombreUsuario, String token, String baseUrl) {
        String resetUrl = baseUrl + "/reset-password?token=" + token;
        log.warn("(Mail no configurado) Se enviaría recuperación a {} - Token: {} - Link: {}", para, token, resetUrl);
    }

    @Override
    public void enviarAlertaSuscripcionVencida(String para, String nombreCliente, String sucursal, LocalDate fechaCaducidad) {
        log.warn("(Mail no configurado) Se enviaría alerta suscripción vencida a {} - Cliente: {} - Sucursal: {} - Caducidad: {}",
                para, nombreCliente, sucursal, fechaCaducidad);
    }
}
