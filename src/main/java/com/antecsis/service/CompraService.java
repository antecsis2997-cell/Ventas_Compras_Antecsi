package com.antecsis.service;

import java.util.List;

import com.antecsis.dto.compra.CompraRequestDTO;
import com.antecsis.dto.compra.CompraResponseDTO;

public interface CompraService {
    CompraResponseDTO crear(CompraRequestDTO dto);
    List<CompraResponseDTO> listar();
    CompraResponseDTO obtenerPorId(Long id);
}
