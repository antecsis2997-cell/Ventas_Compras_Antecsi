package com.antecsis.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.entity.Usuario;
import com.antecsis.repository.UsuarioRepository;

/**
 * Carga usuarios desde la base de datos para Spring Security.
 * Evita que Spring cree el usuario por defecto con contraseña aleatoria.
 * Usado implícitamente por la auto-configuración; el flujo JWT no lo invoca.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (usuario.getRol() != null) {
            String nombreRol = usuario.getRol().getNombre();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + nombreRol));
            if (AccesoUsuario.esSuperadmin(usuario) && RolNombre.SUPERUSUARIO.equals(nombreRol)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + RolNombre.SUPERADMIN));
            }
        }
        if (authorities.isEmpty()) {
            authorities = Collections.emptyList();
        }

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .build();
    }
}
