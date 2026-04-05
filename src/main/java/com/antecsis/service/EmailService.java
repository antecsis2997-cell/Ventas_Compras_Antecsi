package com.antecsis.service;

import java.time.LocalDate;

/**
 * Servicio para envío de correos.
 */
public interface EmailService {
    void enviarRecuperacionContrasena(String para, String nombreUsuario, String token, String baseUrl);
    void enviarAlertaSuscripcionVencida(String para, String nombreCliente, String sucursal, LocalDate fechaCaducidad);

    /** Envía el JWT de licencia al administrador del plan. */
    void enviarLicenciaPlan(String para, String nombreCliente, String planEtiqueta, String tokenLicencia, String urlActivacion);

    /** Aviso de suscripción/licencia que vence en los próximos días. */
    void enviarAlertaSuscripcionPorVencer(String para, String nombreCliente, String sucursal,
                                          LocalDate fechaCaducidad, int diasRestantes);

    /** Proveedor pasó a deshabilitado (soft delete). */
    void enviarAlertaProveedorDeshabilitado(String para, String nombreProveedor, String nombreSede);
}
