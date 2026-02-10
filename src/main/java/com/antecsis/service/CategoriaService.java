package com.antecsis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.antecsis.dto.categoria.CategoriaRequestDTO;
import com.antecsis.dto.categoria.CategoriaResponseDTO;

public interface CategoriaService {
	CategoriaResponseDTO crear(CategoriaRequestDTO dto);
    Page<CategoriaResponseDTO> listar(Pageable pageable);
    CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO dto);
    void eliminar(Long id);
}
