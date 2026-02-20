package com.antecsis.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.antecsis.dto.login.MeResponseDTO;
import com.antecsis.entity.Usuario;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public String login(String username, String password) {
        Usuario user = usuarioRepo.findByUsername(username)
            .orElseThrow(() -> new BusinessException("Usuario no existe"));

        if (!user.getActivo()) {
            throw new BusinessException("Usuario desactivado");
        }

        if (!encoder.matches(password, user.getPassword())) {
            throw new BusinessException("Credenciales inválidas");
        }

        log.info("Login exitoso para usuario: {}", username);

        return jwtUtil.generarToken(
                user.getUsername(),
                user.getRol().getNombre()
        );
    }

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
        return new MeResponseDTO(user.getUsername(), rolNombre, sedeId, sedeNombre);
    }
}
