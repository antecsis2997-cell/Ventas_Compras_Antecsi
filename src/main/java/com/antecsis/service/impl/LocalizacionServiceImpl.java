package com.antecsis.service.impl;

import com.antecsis.dto.localizacion.LocalizacionRequestDTO;
import com.antecsis.dto.localizacion.LocalizacionResponseDTO;
import com.antecsis.entity.Localizacion;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.LocalizacionRepository;
import com.antecsis.service.LocalizacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocalizacionServiceImpl implements LocalizacionService {

    private final LocalizacionRepository repository;

    @Override
    @Transactional
    public LocalizacionResponseDTO crear(LocalizacionRequestDTO dto) {
        Localizacion l = new Localizacion();
        l.setDescripcionLocal(dto.getDescripcionLocal());
        l.setImagenUrl(dto.getImagenUrl());
        Localizacion guardado = repository.save(l);
        return toDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LocalizacionResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public LocalizacionResponseDTO obtenerPorId(Long id) {
        Localizacion l = repository.findById(id).orElseThrow(() -> new BusinessException("Localizacion no existe"));
        return toDTO(l);
    }

    @Override
    @Transactional
    public LocalizacionResponseDTO actualizar(Long id, LocalizacionRequestDTO dto) {
        Localizacion l = repository.findById(id).orElseThrow(() -> new BusinessException("Localizacion no existe"));
        l.setDescripcionLocal(dto.getDescripcionLocal());
        l.setImagenUrl(dto.getImagenUrl());
        return toDTO(repository.save(l));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!repository.existsById(id)) throw new BusinessException("Localizacion no existe");
        repository.deleteById(id);
    }

    private LocalizacionResponseDTO toDTO(Localizacion l) {
        return new LocalizacionResponseDTO(l.getId(), l.getDescripcionLocal(), l.getImagenUrl());
    }
}
