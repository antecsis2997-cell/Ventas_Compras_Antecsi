package com.antecsis.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.dto.categoria.CategoriaRequestDTO;
import com.antecsis.dto.categoria.CategoriaResponseDTO;
import com.antecsis.entity.Categoria;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.CategoriaRepository;
import com.antecsis.service.CategoriaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository repo;

    @Override
    @Transactional
    public CategoriaResponseDTO crear(CategoriaRequestDTO dto) {
        if (repo.existsByNombre(dto.getNombre())) {
            throw new BusinessException("Categoría ya existe");
        }
        Categoria c = repo.save(new Categoria(null, dto.getNombre()));
        return new CategoriaResponseDTO(c.getId(), c.getNombre());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoriaResponseDTO> listar(Pageable pageable) {
        return repo.findAll(pageable)
                .map(c -> new CategoriaResponseDTO(c.getId(), c.getNombre()));
    }

    @Override
    @Transactional
    public CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO dto) {
        Categoria c = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Categoría no existe"));
        c.setNombre(dto.getNombre());
        repo.save(c);
        return new CategoriaResponseDTO(c.getId(), c.getNombre());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new BusinessException("Categoría no existe");
        }
        repo.deleteById(id);
    }
}
