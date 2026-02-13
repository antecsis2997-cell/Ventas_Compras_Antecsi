package com.antecsis.service;

import com.antecsis.dto.sector.SectorRequestDTO;
import com.antecsis.dto.sector.SectorResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SectorService {
    SectorResponseDTO crear(SectorRequestDTO dto);
    Page<SectorResponseDTO> listar(Pageable pageable);
    SectorResponseDTO obtenerPorId(Long id);
    SectorResponseDTO actualizar(Long id, SectorRequestDTO dto);
    void eliminar(Long id);
}
