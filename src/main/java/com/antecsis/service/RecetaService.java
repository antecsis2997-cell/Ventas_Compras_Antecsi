package com.antecsis.service;

import com.antecsis.dto.receta.RecetaRequestDTO;
import com.antecsis.dto.receta.RecetaResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RecetaService {
    RecetaResponseDTO crear(RecetaRequestDTO dto);
    Page<RecetaResponseDTO> listar(Pageable pageable);
    RecetaResponseDTO obtenerPorId(Long id);
}

