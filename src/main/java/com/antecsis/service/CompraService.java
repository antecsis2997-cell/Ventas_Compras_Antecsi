package com.antecsis.service;

import com.antecsis.dto.CompraRequestDTO;
import com.antecsis.dto.CompraResponseDTO;

import java.util.List;

public interface CompraService {
    CompraResponseDTO crear(CompraRequestDTO dto);
    List<CompraResponseDTO> listar();
    CompraResponseDTO obtenerPorId(Long id);
}
