package com.antecsis.service;

import com.antecsis.dto.mensaje.MensajeRequestDTO;
import com.antecsis.dto.mensaje.MensajeResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MensajeService {
    MensajeResponseDTO crear(MensajeRequestDTO dto);
    Page<MensajeResponseDTO> listar(Pageable pageable);
    Page<MensajeResponseDTO> listarPorReceptor(String nombreReceptor, Pageable pageable);
    MensajeResponseDTO obtenerPorId(Long id);
}
