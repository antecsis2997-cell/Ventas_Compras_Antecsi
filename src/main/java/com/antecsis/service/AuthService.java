package com.antecsis.service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.dto.login.LoginResponseDTO;
import com.antecsis.dto.login.MeResponseDTO;
import com.antecsis.entity.Modulo;
import com.antecsis.entity.RefreshToken;
import com.antecsis.entity.Usuario;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.RefreshTokenRepository;
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
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration:3600000}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshTokenExpirationMs;

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
