package com.antecsis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.antecsis.dto.proveedor.ProveedorRequestDTO;
import com.antecsis.dto.proveedor.ProveedorResponseDTO;

public interface ProveedorService {
	ProveedorResponseDTO crear(ProveedorRequestDTO dto);
	Page<ProveedorResponseDTO> listar(Pageable pageable);
	ProveedorResponseDTO obtenerPorId(Long id);
	ProveedorResponseDTO actualizar(Long id, ProveedorRequestDTO dto);
	void eliminar(Long id);
}
