package com.antecsis.service;

import java.util.List;

import com.antecsis.dto.metodopago.MetodoPagoRequestDTO;
import com.antecsis.dto.metodopago.MetodoPagoResponseDTO;

public interface MetodoPagoService {
    MetodoPagoResponseDTO crear(MetodoPagoRequestDTO dto);
    List<MetodoPagoResponseDTO> listar();
    MetodoPagoResponseDTO obtenerPorId(Long id);
    void eliminar(Long id);
}
