package com.antecsis.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.dto.categoria.CategoriaRequestDTO;
import com.antecsis.dto.categoria.CategoriaResponseDTO;
import com.antecsis.entity.Categoria;
import com.antecsis.entity.Sector;
import com.antecsis.entity.Usuario;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.CategoriaRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.service.CategoriaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository repo;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public CategoriaResponseDTO crear(CategoriaRequestDTO dto) {
        Usuario usuario = obtenerUsuarioAutenticado();
        Long sectorId = usuario.getSede() != null ? usuario.getSede().getId() : null;

        if (sectorId != null) {
            if (repo.existsByNombreAndSectorId(dto.getNombre(), sectorId)) {
                throw new BusinessException("Categoría ya existe en tu bodega");
            }
        } else {
            if (repo.existsByNombre(dto.getNombre())) {
                throw new BusinessException("Categoría ya existe");
            }
        }

        Categoria c = new Categoria();
        c.setNombre(dto.getNombre());
        c.setSector(usuario.getSede());
        Categoria guardada = repo.save(c);
        return new CategoriaResponseDTO(guardada.getId(), guardada.getNombre());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoriaResponseDTO> listar(Pageable pageable) {
        Long sectorId = obtenerSectorIdAutenticado();
        if (sectorId != null) {
            return repo.findBySectorId(sectorId, pageable)
                    .map(c -> new CategoriaResponseDTO(c.getId(), c.getNombre()));
        }
        return repo.findAll(pageable)
                .map(c -> new CategoriaResponseDTO(c.getId(), c.getNombre()));
    }

    @Override
    @Transactional
    public CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO dto) {
        Categoria c = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Categoría no existe"));
        verificarAccesoSector(c.getSector());
        c.setNombre(dto.getNombre());
        repo.save(c);
        return new CategoriaResponseDTO(c.getId(), c.getNombre());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Categoria c = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Categoría no existe"));
        verificarAccesoSector(c.getSector());
        repo.deleteById(id);
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
}
