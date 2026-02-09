package com.antecsis.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.antecsis.dto.usuario.UsuarioCreateRequest;
import com.antecsis.entity.Rol;
import com.antecsis.entity.Usuario;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.RolRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService{
	
	private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

	@Override
	public void crearUsuario(UsuarioCreateRequest dto) {
		if (usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new BusinessException("El usuario ya existe");
        }

        Rol rol = rolRepository.findByNombre(dto.getRol())
                .orElseThrow(() -> new BusinessException("Rol no válido"));

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(rol);
        usuario.setActivo(true);

        usuarioRepository.save(usuario);
	}

}
