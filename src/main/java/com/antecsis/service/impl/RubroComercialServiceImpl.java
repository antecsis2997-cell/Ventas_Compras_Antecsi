package com.antecsis.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.dto.rubro.RubroActivoPatchDTO;
import com.antecsis.dto.rubro.RubroComercialDTO;
import com.antecsis.entity.RubroComercial;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.RubroComercialRepository;
import com.antecsis.service.RubroComercialService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RubroComercialServiceImpl implements RubroComercialService {

    private final RubroComercialRepository repo;

    @Override
    @Transactional(readOnly = true)
    public List<RubroComercialDTO> listarActivos() {
        return repo.findByActivoTrueOrderByOrdenAsc().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RubroComercialDTO> listarTodos() {
        return repo.findAllByOrderByOrdenAsc().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public RubroComercialDTO actualizarActivo(Long id, RubroActivoPatchDTO dto) {
        RubroComercial r = repo.findById(id).orElseThrow(() -> new BusinessException("Rubro no encontrado"));
        r.setActivo(Boolean.TRUE.equals(dto.activo()));
        return toDto(repo.save(r));
    }

    private RubroComercialDTO toDto(RubroComercial r) {
        return new RubroComercialDTO(r.getId(), r.getCodigo(), r.getNombre(), r.isActivo(), r.getOrden());
    }
}
