package com.antecsis.service;

import com.antecsis.dto.InventarioResponseDTO;

import java.util.List;

public interface InventarioService {
    List<InventarioResponseDTO> listarTodo();
    List<InventarioResponseDTO> stockBajo(Integer limite);
}
