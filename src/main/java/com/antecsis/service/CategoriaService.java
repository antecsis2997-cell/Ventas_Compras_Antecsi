package com.antecsis.service;

import java.util.List;

import com.antecsis.dto.categoria.CategoriaRequestDTO;
import com.antecsis.dto.categoria.CategoriaResponseDTO;

public interface CategoriaService {
	CategoriaResponseDTO crear(CategoriaRequestDTO dto);
    List<CategoriaResponseDTO> listar();
    CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO dto);
    void eliminar(Long id);
}
