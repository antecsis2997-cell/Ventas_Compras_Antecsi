package com.antecsis.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import com.antecsis.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.mail.host")
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@antecsis.com}")
    private String from;

    @Override
    public void enviarRecuperacionContrasena(String para, String nombreUsuario, String token, String baseUrl) {
        String resetUrl = baseUrl + "/reset-password?token=" + token;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(para);
        msg.setSubject("Recuperar contraseña - AnTecsis");
        msg.setText("Hola " + nombreUsuario + ",\n\n"
                + "Has solicitado recuperar tu contraseña.\n\n"
                + "Haz clic en el siguiente enlace para crear una nueva contraseña (válido por 1 hora):\n"
                + resetUrl + "\n\n"
                + "Si no solicitaste este correo, ignóralo. Tu contraseña no cambiará.\n\n"
                + "— Equipo AnTecsis");

        try {
            mailSender.send(msg);
            log.info("Email de recuperación enviado a: {}", para);
        } catch (Exception e) {
            log.error("Error al enviar email de recuperación a {}: {}", para, e.getMessage());
            throw e;
        }
    }

    @Override
    public void enviarAlertaSuscripcionVencida(String para, String nombreCliente, String sucursal, LocalDate fechaCaducidad) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(para);
        msg.setSubject("Alerta: Suscripción vencida - " + nombreCliente);
        msg.setText("Estimado/a,\n\n"
                + "La suscripción del cliente " + nombreCliente + " (Sucursal: " + sucursal + ") ha vencido.\n\n"
                + "Fecha de caducidad: " + fechaCaducidad + "\n\n"
                + "Por favor, renovar la licencia.\n\n"
                + "— Sistema AnTecsis");

        try {
            mailSender.send(msg);
            log.info("Alerta suscripción vencida enviada a: {} (cliente: {})", para, nombreCliente);
        } catch (Exception e) {
            log.error("Error al enviar alerta suscripción a {}: {}", para, e.getMessage());
            throw e;
        }
    }

    @Override
    public void enviarLicenciaPlan(String para, String nombreCliente, String planEtiqueta, String tokenLicencia, String urlActivacion) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(para);
        msg.setSubject("Su licencia AnTecsis — Plan " + planEtiqueta);
        msg.setText("Hola " + nombreCliente + ",\n\n"
                + "Gracias por adquirir el plan " + planEtiqueta + ".\n\n"
                + "Su clave de licencia (cópiela completa):\n\n"
                + tokenLicencia + "\n\n"
                + "Active la licencia en la aplicación:\n"
                + "Ajustes → Cuenta → Licencia (o " + urlActivacion + ")\n\n"
                + "— Equipo AnTecsis");

        try {
            mailSender.send(msg);
            log.info("Licencia de plan enviada a: {}", para);
        } catch (Exception e) {
            log.error("Error al enviar licencia a {}: {}", para, e.getMessage());
            throw e;
        }
    }

    @Override
    public void enviarAlertaSuscripcionPorVencer(String para, String nombreCliente, String sucursal,
                                                 LocalDate fechaCaducidad, int diasRestantes) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(para);
        msg.setSubject("Importante: su licencia AnTecsis vence en " + diasRestantes + " día(s)");
        msg.setText("Estimado/a " + nombreCliente + ",\n\n"
                + "La suscripción/licencia de la sucursal \"" + sucursal + "\" vencerá el "
                + fechaCaducidad + " (en " + diasRestantes + " día(s)).\n\n"
                + "Renueve a tiempo para evitar interrupciones.\n\n"
                + "— Sistema AnTecsis");
        try {
            mailSender.send(msg);
            log.info("Alerta próximo vencimiento enviada a {}", para);
        } catch (Exception e) {
            log.error("Error al enviar alerta próximo vencimiento a {}: {}", para, e.getMessage());
            throw e;
        }
    }

    @Override
    public void enviarAlertaProveedorDeshabilitado(String para, String nombreProveedor, String nombreSede) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(para);
        msg.setSubject("Importante: proveedor deshabilitado — " + nombreProveedor);
        msg.setText("Estimado/a administrador/a,\n\n"
                + "El proveedor \"" + nombreProveedor + "\" ha sido deshabilitado en la sede \"" + nombreSede + "\".\n\n"
                + "Revise el módulo Proveedores si necesita reactivarlo o reemplazarlo.\n\n"
                + "— Sistema AnTecsis");
        try {
            mailSender.send(msg);
            log.info("Alerta proveedor deshabilitado enviada a {}", para);
        } catch (Exception e) {
            log.error("Error al enviar alerta proveedor a {}: {}", para, e.getMessage());
            throw e;
        }
    }
}
