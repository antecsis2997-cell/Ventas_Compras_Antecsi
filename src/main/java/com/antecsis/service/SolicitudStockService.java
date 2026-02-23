package com.antecsis.service;

import com.antecsis.dto.solicitudstock.SolicitudStockRequestDTO;
import com.antecsis.dto.solicitudstock.SolicitudStockResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SolicitudStockService {
    SolicitudStockResponseDTO crear(SolicitudStockRequestDTO dto);
    Page<SolicitudStockResponseDTO> listar(Pageable pageable);
    SolicitudStockResponseDTO aprobar(Long id);
    SolicitudStockResponseDTO desaprobar(Long id);
}
