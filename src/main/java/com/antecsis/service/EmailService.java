package com.antecsis.service;

import java.time.LocalDate;

/**
 * Servicio para envío de correos.
 */
public interface EmailService {
    void enviarRecuperacionContrasena(String para, String nombreUsuario, String token, String baseUrl);
    void enviarAlertaSuscripcionVencida(String para, String nombreCliente, String sucursal, LocalDate fechaCaducidad);
}
