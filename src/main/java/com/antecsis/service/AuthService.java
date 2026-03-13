package com.antecsis.service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.dto.login.AprobarRecuperacionResponseDTO;
import com.antecsis.dto.login.LoginResponseDTO;
import com.antecsis.dto.login.MeResponseDTO;
import com.antecsis.dto.login.SolicitudRecuperacionResponseDTO;
import com.antecsis.entity.Modulo;
import com.antecsis.entity.PasswordResetToken;
import com.antecsis.entity.RefreshToken;
import com.antecsis.entity.SolicitudRecuperacionContrasena;
import com.antecsis.entity.Usuario;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.PasswordResetTokenRepository;
import com.antecsis.repository.RefreshTokenRepository;
import com.antecsis.repository.SolicitudRecuperacionRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepo;
    private final RefreshTokenRepository refreshTokenRepo;
    private final PasswordResetTokenRepository passwordResetTokenRepo;
    private final SolicitudRecuperacionRepository solicitudRecuperacionRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Value("${jwt.expiration:3600000}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshTokenExpirationMs;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    private static final long PASSWORD_RESET_EXPIRATION_MS = 3600000; // 1 hora

    /** Solicitud de recuperación (sin enviar email): crea solicitud PENDIENTE para que Admin/Soporte apruebe. */
    @Transactional
    public void solicitarRecuperacion(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new BusinessException("Correo es obligatorio");
        }
        Usuario user = usuarioRepo.findByCorreoIgnoreCase(correo.trim()).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getActivo())) {
            return; // No revelar si el correo existe
        }
        if (user.getSede() == null) {
            return; // Usuario sin sede (ej. superadmin) no puede solicitar por este flujo
        }
        solicitudRecuperacionRepo.deleteByUsuario_IdAndEstado(user.getId(), SolicitudRecuperacionContrasena.Estado.PENDIENTE);
        SolicitudRecuperacionContrasena sol = new SolicitudRecuperacionContrasena();
        sol.setUsuario(user);
        sol.setEstado(SolicitudRecuperacionContrasena.Estado.PENDIENTE);
        sol.setFechaSolicitud(Instant.now());
        solicitudRecuperacionRepo.save(sol);
        log.info("Solicitud de recuperación creada para usuario: {}", user.getUsername());
    }

    /** Listar solicitudes pendientes: SUPERUSUARIO ve todas; Admin/Soporte solo las de su sector. */
    @Transactional(readOnly = true)
    public List<SolicitudRecuperacionResponseDTO> listarSolicitudesPendientes() {
        Usuario actual = obtenerUsuarioActual();
        validarAdminOSoporteDeSede(actual);
        List<SolicitudRecuperacionContrasena> list;
        if ("SUPERUSUARIO".equals(actual.getRol() != null ? actual.getRol().getNombre() : null)) {
            list = solicitudRecuperacionRepo.findByEstadoOrderByFechaSolicitudDesc(
                    SolicitudRecuperacionContrasena.Estado.PENDIENTE);
        } else if (actual.getSede() != null) {
            list = solicitudRecuperacionRepo.findByUsuario_SedeAndEstadoOrderByFechaSolicitudDesc(
                    actual.getSede(), SolicitudRecuperacionContrasena.Estado.PENDIENTE);
        } else {
            return List.of();
        }
        return list.stream().map(this::toSolicitudDTO).toList();
    }

    /** Admin/Soporte aprueba: envía email de recuperación al usuario. */
    @Transactional
    public AprobarRecuperacionResponseDTO aprobarRecuperacion(Long solicitudId) {
        Usuario actual = obtenerUsuarioActual();
        validarAdminOSoporteDeSede(actual);
        SolicitudRecuperacionContrasena sol = solicitudRecuperacionRepo.findById(solicitudId)
                .orElseThrow(() -> new BusinessException("Solicitud no encontrada"));
        if (sol.getEstado() != SolicitudRecuperacionContrasena.Estado.PENDIENTE) {
            throw new BusinessException("La solicitud ya fue procesada");
        }
        String rolActual = actual.getRol() != null ? actual.getRol().getNombre() : null;
        if (!"SUPERUSUARIO".equals(rolActual)
                && (actual.getSede() == null || sol.getUsuario().getSede() == null
                    || !actual.getSede().getId().equals(sol.getUsuario().getSede().getId()))) {
            throw new BusinessException("Solo puede aprobar solicitudes de su sector");
        }
        Usuario user = sol.getUsuario();
        passwordResetTokenRepo.deleteByUsuarioId(user.getId());
        String token = UUID.randomUUID().toString().replace("-", "");
        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(token);
        prt.setUsuario(user);
        Instant now = Instant.now();
        prt.setExpiresAt(now.plusMillis(PASSWORD_RESET_EXPIRATION_MS));
        prt.setCreatedAt(now);
        passwordResetTokenRepo.save(prt);
        String nombre = (user.getNombre() != null ? user.getNombre() : user.getUsername());
        emailService.enviarRecuperacionContrasena(user.getCorreo(), nombre, token, frontendUrl);
        sol.setEstado(SolicitudRecuperacionContrasena.Estado.APROBADO);
        sol.setAprobadoPor(actual);
        sol.setFechaAprobacion(now);
        solicitudRecuperacionRepo.save(sol);
        log.info("Recuperación aprobada por {} para usuario: {}", actual.getUsername(), user.getUsername());
        return new AprobarRecuperacionResponseDTO(
            "Se envió el correo de recuperación correctamente.",
            user.getCorreo());
    }

    /** Admin/Soporte envía correo de recuperación directamente a un usuario (sin solicitud previa). */
    @Transactional
    public AprobarRecuperacionResponseDTO enviarRecuperacionDirecta(Long usuarioId) {
        Usuario actual = obtenerUsuarioActual();
        validarAdminOSoporteDeSede(actual);
        Usuario user = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        if (user.getCorreo() == null || user.getCorreo().isBlank()) {
            throw new BusinessException("El usuario no tiene correo registrado");
        }
        if (!Boolean.TRUE.equals(user.getActivo())) {
            throw new BusinessException("No se puede enviar recuperación a un usuario inactivo");
        }
        String rolActual = actual.getRol() != null ? actual.getRol().getNombre() : null;
        if (!"SUPERUSUARIO".equals(rolActual)
                && (actual.getSede() == null || user.getSede() == null
                    || !actual.getSede().getId().equals(user.getSede().getId()))) {
            throw new BusinessException("Solo puede restablecer contraseña de usuarios de su sector");
        }
        passwordResetTokenRepo.deleteByUsuarioId(user.getId());
        String token = UUID.randomUUID().toString().replace("-", "");
        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(token);
        prt.setUsuario(user);
        Instant now = Instant.now();
        prt.setExpiresAt(now.plusMillis(PASSWORD_RESET_EXPIRATION_MS));
        prt.setCreatedAt(now);
        passwordResetTokenRepo.save(prt);
        String nombre = (user.getNombre() != null ? user.getNombre() : user.getUsername());
        emailService.enviarRecuperacionContrasena(user.getCorreo(), nombre, token, frontendUrl);
        log.info("Recuperación enviada por {} para usuario: {}", actual.getUsername(), user.getUsername());
        return new AprobarRecuperacionResponseDTO("Se envió el correo de recuperación correctamente.", user.getCorreo());
    }

    /** Admin/Soporte rechaza una solicitud. */
    @Transactional
    public void rechazarRecuperacion(Long solicitudId) {
        Usuario actual = obtenerUsuarioActual();
        validarAdminOSoporteDeSede(actual);
        SolicitudRecuperacionContrasena sol = solicitudRecuperacionRepo.findById(solicitudId)
                .orElseThrow(() -> new BusinessException("Solicitud no encontrada"));
        if (sol.getEstado() != SolicitudRecuperacionContrasena.Estado.PENDIENTE) {
            throw new BusinessException("La solicitud ya fue procesada");
        }
        String rolRechazo = actual.getRol() != null ? actual.getRol().getNombre() : null;
        if (!"SUPERUSUARIO".equals(rolRechazo)
                && (actual.getSede() == null || sol.getUsuario().getSede() == null
                    || !actual.getSede().getId().equals(sol.getUsuario().getSede().getId()))) {
            throw new BusinessException("Solo puede rechazar solicitudes de su sector");
        }
        sol.setEstado(SolicitudRecuperacionContrasena.Estado.RECHAZADO);
        sol.setAprobadoPor(actual);
        sol.setFechaAprobacion(Instant.now());
        solicitudRecuperacionRepo.save(sol);
        log.info("Recuperación rechazada por {} para solicitud usuario: {}", actual.getUsername(), sol.getUsuario().getUsername());
    }

    private void validarAdminOSoporteDeSede(Usuario u) {
        String rol = u.getRol() != null ? u.getRol().getNombre() : null;
        if (!"ADMIN".equals(rol) && !"SOPORTE".equals(rol) && !"SUPERUSUARIO".equals(rol)) {
            throw new BusinessException("Solo Admin o Soporte de su sector pueden gestionar solicitudes de recuperación");
        }
    }

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("No autenticado");
        }
        return usuarioRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
    }

    private SolicitudRecuperacionResponseDTO toSolicitudDTO(SolicitudRecuperacionContrasena s) {
        Usuario u = s.getUsuario();
        String nombreCompleto = (u.getNombre() != null ? u.getNombre() : "")
                + (u.getApellido() != null ? " " + u.getApellido() : "").trim();
        if (nombreCompleto.isBlank()) nombreCompleto = u.getUsername();
        return new SolicitudRecuperacionResponseDTO(
            s.getId(), u.getUsername(), nombreCompleto, u.getCorreo(), s.getFechaSolicitud());
    }

    /** Indica si el usuario puede ver el link "Recuperar contraseña" tras fallar login. Público (para el login). */
    @Transactional(readOnly = true)
    public boolean puedeRecuperarContrasena(String username) {
        if (username == null || username.isBlank()) return false;
        return usuarioRepo.findByUsername(username.trim())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .map(u -> Boolean.TRUE.equals(u.getPuedeRecuperarContrasena()))
                .orElse(false);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw new BusinessException("Token inválido");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("La contraseña debe tener al menos 6 caracteres");
        }
        PasswordResetToken prt = passwordResetTokenRepo.findByToken(token.trim())
                .orElseThrow(() -> new BusinessException("Token inválido o expirado"));
        if (prt.isExpired()) {
            passwordResetTokenRepo.delete(prt);
            throw new BusinessException("Token expirado. Solicita nuevamente el enlace.");
        }
        Usuario user = prt.getUsuario();
        user.setPassword(encoder.encode(newPassword));
        usuarioRepo.save(user);
        passwordResetTokenRepo.delete(prt);
        log.info("Contraseña restablecida para usuario: {}", user.getUsername());
    }

    @Transactional
    public LoginResponseDTO login(String username, String password) {
        Usuario user = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario no existe"));

        if (!encoder.matches(password, user.getPassword())) {
            throw new BusinessException("Credenciales inválidas");
        }

        if (!user.getActivo()) {
            throw new BusinessException("Usuario desactivado");
        }

        refreshTokenRepo.deleteByUsuarioId(user.getId());

        String accessToken = jwtUtil.generarToken(user.getUsername(), user.getRol().getNombre());
        RefreshToken refreshToken = crearRefreshToken(user);

        log.info("Login exitoso para usuario: {}", username);

        return new LoginResponseDTO(
                accessToken,
                refreshToken.getToken(),
                accessTokenExpirationMs / 1000
        );
    }

    @Transactional
    public LoginResponseDTO refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepo.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BusinessException("Refresh token inválido"));

        if (refreshToken.isExpired()) {
            refreshTokenRepo.delete(refreshToken);
            throw new BusinessException("Refresh token expirado. Inicie sesión nuevamente.");
        }

        Usuario user = refreshToken.getUsuario();
        if (!Boolean.TRUE.equals(user.getActivo())) {
            refreshTokenRepo.delete(refreshToken);
            throw new BusinessException("Usuario desactivado");
        }

        refreshTokenRepo.delete(refreshToken);
        RefreshToken newRefreshToken = crearRefreshToken(user);
        String accessToken = jwtUtil.generarToken(user.getUsername(), user.getRol().getNombre());

        return new LoginResponseDTO(
                accessToken,
                newRefreshToken.getToken(),
                accessTokenExpirationMs / 1000
        );
    }

    private RefreshToken crearRefreshToken(Usuario usuario) {
        Instant now = Instant.now();
        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString());
        rt.setUsuario(usuario);
        rt.setExpiresAt(now.plusMillis(refreshTokenExpirationMs));
        rt.setCreatedAt(now);
        return refreshTokenRepo.save(rt);
    }

    @Transactional(readOnly = true)
    public MeResponseDTO getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("No autenticado");
        }
        Usuario user = usuarioRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        Long sedeId = user.getSede() != null ? user.getSede().getId() : null;
        String sedeNombre = user.getSede() != null ? user.getSede().getNombreSector() : null;
        String rolNombre = user.getRol() != null ? user.getRol().getNombre() : null;

        Set<String> modulos;
        if ("SUPERUSUARIO".equals(rolNombre)) {
            modulos = Set.of("*");
        } else {
            modulos = user.getModulos().stream()
                    .map(Modulo::getCodigo)
                    .collect(Collectors.toSet());
        }

        return new MeResponseDTO(user.getUsername(), user.getNombre(), user.getApellido(),
                rolNombre, sedeId, sedeNombre, modulos);
    }
}
