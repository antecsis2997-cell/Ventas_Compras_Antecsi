package com.antecsis.service;

import com.antecsis.dto.solicitud.SolicitudProductoRequestDTO;
import com.antecsis.dto.solicitud.SolicitudProductoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SolicitudProductoService {
    SolicitudProductoResponseDTO crear(SolicitudProductoRequestDTO dto);
    Page<SolicitudProductoResponseDTO> listar(Pageable pageable);
    List<SolicitudProductoResponseDTO> listarPendientes();
    SolicitudProductoResponseDTO obtenerPorId(Long id);
    SolicitudProductoResponseDTO marcarAtendida(Long id);
    void eliminar(Long id);
}
