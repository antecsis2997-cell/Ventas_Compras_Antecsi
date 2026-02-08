package com.antecsis.service;

import com.antecsis.dto.VentaRequestDTO;
import com.antecsis.dto.VentaResponseDTO;

import java.util.List;

public interface VentaService {
    VentaResponseDTO crear(VentaRequestDTO dto);

    List<VentaResponseDTO> listar();

    VentaResponseDTO obtenerPorId(Long id);
}
