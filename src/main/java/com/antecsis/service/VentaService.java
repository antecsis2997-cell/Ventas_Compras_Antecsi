package com.antecsis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.antecsis.dto.venta.VentaRequestDTO;
import com.antecsis.dto.venta.VentaResponseDTO;

public interface VentaService {
    VentaResponseDTO crear(VentaRequestDTO dto);
    Page<VentaResponseDTO> listar(Pageable pageable);
    VentaResponseDTO obtenerPorId(Long id);
    VentaResponseDTO anular(Long id);
}
