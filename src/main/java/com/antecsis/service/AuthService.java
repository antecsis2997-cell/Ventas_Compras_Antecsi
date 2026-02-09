package com.antecsis.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.antecsis.entity.Usuario;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.security.JwtUtil;

@Service
public class AuthService {
	private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder encoder;

    public AuthService(UsuarioRepository usuarioRepo, PasswordEncoder encoder) {
        this.usuarioRepo = usuarioRepo;
        this.encoder = encoder;
    }

    public String login(String username, String password) {
        Usuario user = usuarioRepo.findByUsername(username)
            .orElseThrow(() -> new BusinessException("Usuario no existe"));

        if (!encoder.matches(password, user.getPassword())) {
            throw new BusinessException("Credenciales inválidas");
        }
        
        return JwtUtil.generarToken(
                user.getUsername(),
                user.getRol().getNombre()
        );
    }
}
