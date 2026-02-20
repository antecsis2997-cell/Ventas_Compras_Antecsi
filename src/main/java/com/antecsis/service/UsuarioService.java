package com.antecsis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.antecsis.dto.usuario.UsuarioCreateRequest;
import com.antecsis.dto.usuario.UsuarioResponseDTO;
import com.antecsis.dto.usuario.UsuarioUpdateRequest;

public interface UsuarioService {
	void crearUsuario(UsuarioCreateRequest dto);
	Page<UsuarioResponseDTO> listar(Pageable pageable);
	UsuarioResponseDTO obtenerPorId(Long id);
	UsuarioResponseDTO actualizar(Long id, UsuarioUpdateRequest dto);
	void eliminar(Long id);
    void actualizarActivo(Long id, boolean activo);
}
