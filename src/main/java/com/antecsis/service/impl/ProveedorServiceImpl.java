package com.antecsis.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.dto.proveedor.ProveedorRequestDTO;
import com.antecsis.dto.proveedor.ProveedorResponseDTO;
import com.antecsis.entity.Proveedor;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.ProveedorRepository;
import com.antecsis.service.ProveedorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;

    @Override
    @Transactional
    public ProveedorResponseDTO crear(ProveedorRequestDTO dto) {
        if (proveedorRepository.existsByNombre(dto.getNombre())) {
            throw new BusinessException("Ya existe un proveedor con ese nombre");
        }

        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(dto.getNombre());
        proveedor.setRuc(dto.getRuc());
        proveedor.setEmail(dto.getEmail());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setDireccion(dto.getDireccion());
        proveedor.setActivo(true);

        Proveedor guardado = proveedorRepository.save(proveedor);
        return toResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProveedorResponseDTO> listar(Pageable pageable) {
        return proveedorRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponseDTO obtenerPorId(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Proveedor no existe"));
        return toResponseDTO(proveedor);
    }

    @Override
    @Transactional
    public ProveedorResponseDTO actualizar(Long id, ProveedorRequestDTO dto) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Proveedor no existe"));

        proveedor.setNombre(dto.getNombre());
        proveedor.setRuc(dto.getRuc());
        proveedor.setEmail(dto.getEmail());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setDireccion(dto.getDireccion());

        Proveedor guardado = proveedorRepository.save(proveedor);
        return toResponseDTO(guardado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Proveedor no existe"));
        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
    }

    private ProveedorResponseDTO toResponseDTO(Proveedor p) {
        return new ProveedorResponseDTO(
                p.getId(),
                p.getNombre(),
                p.getRuc(),
                p.getEmail(),
                p.getTelefono(),
                p.getDireccion(),
                p.getActivo()
        );
    }
}
