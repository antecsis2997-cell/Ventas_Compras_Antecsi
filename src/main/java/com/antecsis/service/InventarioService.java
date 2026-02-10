package com.antecsis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.antecsis.dto.InventarioResponseDTO;

public interface InventarioService {
    Page<InventarioResponseDTO> listarTodo(Pageable pageable);
    Page<InventarioResponseDTO> stockBajo(Integer limite, Pageable pageable);
}
