package com.antecsis.service;

import java.util.List;

import com.antecsis.dto.venta.VentaRequestDTO;
import com.antecsis.dto.venta.VentaResponseDTO;

public interface VentaService {
    VentaResponseDTO crear(VentaRequestDTO dto);
    List<VentaResponseDTO> listar();
    VentaResponseDTO obtenerPorId(Long id);
}
