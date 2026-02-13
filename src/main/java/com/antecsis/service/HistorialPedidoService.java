package com.antecsis.service;

import com.antecsis.dto.historial.HistorialPedidoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface HistorialPedidoService {
    Page<HistorialPedidoResponseDTO> listar(Pageable pageable);
    List<HistorialPedidoResponseDTO> listarPorFechas(LocalDate fechaInicio, LocalDate fechaFin);
}
