package com.antecsis.service;

import java.util.List;

import com.antecsis.dto.rubro.RubroActivoPatchDTO;
import com.antecsis.dto.rubro.RubroComercialDTO;

public interface RubroComercialService {
    List<RubroComercialDTO> listarActivos();

    List<RubroComercialDTO> listarTodos();

    RubroComercialDTO actualizarActivo(Long id, RubroActivoPatchDTO dto);
}
