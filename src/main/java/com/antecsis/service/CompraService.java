package com.antecsis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.antecsis.dto.compra.CompraRequestDTO;
import com.antecsis.dto.compra.CompraResponseDTO;

public interface CompraService {
    CompraResponseDTO crear(CompraRequestDTO dto);
    Page<CompraResponseDTO> listar(Pageable pageable);
    CompraResponseDTO obtenerPorId(Long id);
    CompraResponseDTO anular(Long id);
}
