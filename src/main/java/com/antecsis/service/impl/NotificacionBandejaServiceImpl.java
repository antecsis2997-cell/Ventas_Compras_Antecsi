package com.antecsis.service.impl;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.dto.notificacion.NotificacionBandejaDTO;
import com.antecsis.entity.NotificacionBandeja;
import com.antecsis.entity.Usuario;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.NotificacionBandejaRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.service.NotificacionBandejaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificacionBandejaServiceImpl implements NotificacionBandejaService {

    private final NotificacionBandejaRepository repo;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public void registrar(String emailDestino, String tipo, String titulo, String cuerpoResumen) {
        if (emailDestino == null || emailDestino.isBlank()) {
            return;
        }
        NotificacionBandeja n = NotificacionBandeja.builder()
                .emailDestino(emailDestino.trim().toLowerCase())
                .tipo(tipo)
                .titulo(titulo)
                .cuerpoResumen(cuerpoResumen != null && cuerpoResumen.length() > 1900
                        ? cuerpoResumen.substring(0, 1900) + "…"
                        : cuerpoResumen)
                .build();
        repo.save(n);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionBandejaDTO> listarParaUsuarioAutenticado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario u = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        String email = u.getCorreo();
        if (email == null || email.isBlank()) {
            return List.of();
        }
        return repo.findByEmailDestinoIgnoreCaseOrderByCreatedAtDesc(email.trim()).stream()
                .map(n -> new NotificacionBandejaDTO(n.getId(), n.getTipo(), n.getTitulo(), n.getCuerpoResumen(), n.getCreatedAt()))
                .toList();
    }
}
