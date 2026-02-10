package com.antecsis.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.dto.metodopago.MetodoPagoRequestDTO;
import com.antecsis.dto.metodopago.MetodoPagoResponseDTO;
import com.antecsis.entity.MetodoPago;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.MetodoPagoRepository;
import com.antecsis.service.MetodoPagoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetodoPagoServiceImpl implements MetodoPagoService {

    private final MetodoPagoRepository repository;

    @Override
    @Transactional
    public MetodoPagoResponseDTO crear(MetodoPagoRequestDTO dto) {
        if (repository.existsByNombre(dto.getNombre())) {
            throw new BusinessException("Ya existe un método de pago con ese nombre");
        }

        MetodoPago mp = new MetodoPago();
        mp.setNombre(dto.getNombre().toUpperCase());
        mp.setActivo(true);

        MetodoPago guardado = repository.save(mp);
        return toResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetodoPagoResponseDTO> listar() {
        return repository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MetodoPagoResponseDTO obtenerPorId(Long id) {
        MetodoPago mp = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Método de pago no existe"));
        return toResponseDTO(mp);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        MetodoPago mp = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Método de pago no existe"));
        mp.setActivo(false);
        repository.save(mp);
    }

    private MetodoPagoResponseDTO toResponseDTO(MetodoPago mp) {
        return new MetodoPagoResponseDTO(mp.getId(), mp.getNombre(), mp.getActivo());
    }
}
