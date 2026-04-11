package com.antecsis.service.impl;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.dto.licencia.ActivarLicenciaRequestDTO;
import com.antecsis.dto.licencia.LicenciaEstadoResponseDTO;
import com.antecsis.entity.ActivacionLicencia;
import com.antecsis.entity.Suscripcion;
import com.antecsis.entity.Usuario;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.ActivacionLicenciaRepository;
import com.antecsis.repository.SuscripcionRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.security.AccesoUsuario;
import com.antecsis.security.LicenseJwtUtil;
import com.antecsis.service.LicenciaCuentaService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LicenciaCuentaServiceImpl implements LicenciaCuentaService {

    private final UsuarioRepository usuarioRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final ActivacionLicenciaRepository activacionLicenciaRepository;
    private final LicenseJwtUtil licenseJwtUtil;

    @Override
    @Transactional(readOnly = true)
    public LicenciaEstadoResponseDTO estadoMiCuenta() {
        Usuario u = usuarioActual();
        boolean superPlataforma = AccesoUsuario.esSuperadmin(u);

        if (u.getSede() == null) {
            String msg = superPlataforma
                    ? "Como SUPERADMIN no tiene sede asignada; la licencia se asocia a la sucursal de la suscripción."
                    : "Su usuario no tiene sede asignada. Contacte al administrador.";
            return new LicenciaEstadoResponseDTO(false, null, null, "N/D", null, false, null, msg);
        }

        var susOpt = suscripcionRepository.findFirstBySector_IdAndEstadoOrderByIdDesc(u.getSede().getId(), "PAGADO");
        if (susOpt.isEmpty()) {
            return new LicenciaEstadoResponseDTO(
                    true, null, null, "SIN_SUSCRIPCION", null, false, null,
                    "No hay suscripción pagada vinculada a su sucursal.");
        }

        Suscripcion s = susOpt.get();
        var actOpt = activacionLicenciaRepository.findBySuscripcion_Id(s.getId());
        boolean activada = actOpt.map(ActivacionLicencia::isActivada).orElse(false);
        boolean vencida = s.isVencida();
        String estado;
        if (vencida) {
            estado = "INHABILITADO";
        } else if (activada) {
            estado = "ACTIVO";
        } else {
            estado = "PENDIENTE_ACTIVACION";
        }

        String rubro = s.getRubroComercial() != null ? s.getRubroComercial().getNombre() : null;
        return new LicenciaEstadoResponseDTO(
                true,
                etiquetaPlan(s.getPaquete()),
                s.getPaquete(),
                estado,
                s.getFechaCaducidad(),
                activada && !vencida,
                rubro,
                vencida ? "La suscripción está vencida." : null
        );
    }

    @Override
    @Transactional
    public void activar(ActivarLicenciaRequestDTO dto) {
        Usuario u = usuarioActual();
        boolean superPlataforma = AccesoUsuario.esSuperadmin(u);

        Claims claims;
        try {
            claims = licenseJwtUtil.parseAndValidate(dto.token());
        } catch (JwtException e) {
            throw new BusinessException("Token de licencia inválido o expirado");
        }

        long suscripcionId = Long.parseLong(claims.getSubject());
        String jti = claims.getId();
        if (jti == null || jti.isBlank()) {
            throw new BusinessException("Token de licencia inválido");
        }

        ActivacionLicencia al = activacionLicenciaRepository.findBySuscripcion_Id(suscripcionId)
                .orElseThrow(() -> new BusinessException("No existe activación para esta licencia"));

        if (!jti.equals(al.getJti())) {
            throw new BusinessException("Token no coincide con la licencia emitida");
        }

        Suscripcion s = al.getSuscripcion();
        if (s.isVencida()) {
            throw new BusinessException("La suscripción asociada está vencida");
        }

        if (!superPlataforma) {
            if (u.getSede() == null) {
                throw new BusinessException("Su usuario no tiene sede asignada");
            }
            if (s.getSector() == null || !u.getSede().getId().equals(s.getSector().getId())) {
                throw new BusinessException("Esta licencia pertenece a otra sucursal");
            }
        }

        if (al.isActivada()) {
            if (al.getActivadaPor() != null && !al.getActivadaPor().getId().equals(u.getId())) {
                throw new BusinessException("La licencia ya fue activada por otro usuario");
            }
            return;
        }

        al.setActivada(true);
        al.setFechaActivacion(LocalDateTime.now());
        al.setActivadaPor(u);
        activacionLicenciaRepository.save(al);
    }

    private Usuario usuarioActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
    }

    private static String etiquetaPlan(String paquete) {
        if (paquete == null) return "—";
        return switch (paquete.toUpperCase()) {
            case "PAQUETE_BASICO", "BASICO" -> "Básico";
            case "PAQUETE_INTERMEDIO", "INTERMEDIO" -> "Intermedio";
            case "PAQUETE_AVANZADO", "AVANZADO" -> "Premium";
            default -> paquete.replace("PAQUETE_", "").replace("_", " ");
        };
    }
}
