package com.antecsis.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.dto.proveedor.ProveedorRequestDTO;
import com.antecsis.dto.proveedor.ProveedorResponseDTO;
import com.antecsis.entity.Proveedor;
import com.antecsis.entity.Sector;
import com.antecsis.entity.Usuario;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.ProveedorRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.service.ProveedorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public ProveedorResponseDTO crear(ProveedorRequestDTO dto) {
        Usuario usuario = obtenerUsuarioAutenticado();
        Long sectorId = usuario.getSede() != null ? usuario.getSede().getId() : null;

        if (sectorId != null) {
            if (proveedorRepository.existsByNombreAndSectorId(dto.nombre(), sectorId)) {
                throw new BusinessException("Ya existe un proveedor con ese nombre en tu bodega");
            }
        } else {
            if (proveedorRepository.existsByNombre(dto.nombre())) {
                throw new BusinessException("Ya existe un proveedor con ese nombre");
            }
        }

        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(dto.nombre());
        proveedor.setRuc(dto.ruc());
        proveedor.setEmail(dto.email());
        proveedor.setTelefono(dto.telefono());
        proveedor.setDireccion(dto.direccion());
        proveedor.setSector(usuario.getSede());
        proveedor.setActivo(true);

        Proveedor guardado = proveedorRepository.save(proveedor);
        return toResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProveedorResponseDTO> listar(Pageable pageable) {
        Long sectorId = obtenerSectorIdAutenticado();
        if (sectorId != null) {
            return proveedorRepository.findBySectorId(sectorId, pageable).map(this::toResponseDTO);
        }
        return proveedorRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponseDTO obtenerPorId(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Proveedor no existe"));
        verificarAccesoSector(proveedor.getSector());
        return toResponseDTO(proveedor);
    }

    @Override
    @Transactional
    public ProveedorResponseDTO actualizar(Long id, ProveedorRequestDTO dto) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Proveedor no existe"));
        verificarAccesoSector(proveedor.getSector());

        proveedor.setNombre(dto.nombre());
        proveedor.setRuc(dto.ruc());
        proveedor.setEmail(dto.email());
        proveedor.setTelefono(dto.telefono());
        proveedor.setDireccion(dto.direccion());

        Proveedor guardado = proveedorRepository.save(proveedor);
        return toResponseDTO(guardado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Proveedor no existe"));
        verificarAccesoSector(proveedor.getSector());
        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
    }

    private void verificarAccesoSector(Sector sectorEntidad) {
        Long sectorIdUsuario = obtenerSectorIdAutenticado();
        if (sectorIdUsuario != null && sectorEntidad != null
                && !sectorIdUsuario.equals(sectorEntidad.getId())) {
            throw new BusinessException("No tiene acceso a este recurso");
        }
    }

    private Long obtenerSectorIdAutenticado() {
        Usuario usuario = obtenerUsuarioAutenticado();
        return usuario.getSede() != null ? usuario.getSede().getId() : null;
    }

    private Usuario obtenerUsuarioAutenticado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario autenticado no encontrado"));
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
