package com.antecsis.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.antecsis.dto.categoria.CategoriaRequestDTO;
import com.antecsis.dto.categoria.CategoriaResponseDTO;
import com.antecsis.entity.Categoria;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.CategoriaRepository;
import com.antecsis.service.CategoriaService;

@Service
public class CategoriaServiceImpl implements CategoriaService{
	
	private final CategoriaRepository repo;

    public CategoriaServiceImpl(CategoriaRepository repo) {
        this.repo = repo;
    }

	@Override
	public CategoriaResponseDTO crear(CategoriaRequestDTO dto) {
		if (repo.existsByNombre(dto.getNombre())) {
            throw new BusinessException("Categoría ya existe");
        }
        Categoria c = repo.save(new Categoria(null, dto.getNombre()));
        return new CategoriaResponseDTO(c.getId(), c.getNombre());
	}

	@Override
	public List<CategoriaResponseDTO> listar() {
		return repo.findAll().stream()
                .map(c -> new CategoriaResponseDTO(c.getId(), c.getNombre()))
                .toList();
	}

	@Override
	public CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO dto) {
		Categoria c = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Categoría no existe"));
        c.setNombre(dto.getNombre());
        repo.save(c);
        return new CategoriaResponseDTO(c.getId(), c.getNombre());
	}

	@Override
	public void eliminar(Long id) {
		repo.deleteById(id);
	}

}
