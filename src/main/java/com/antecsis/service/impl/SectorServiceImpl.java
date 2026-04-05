package com.antecsis.service.impl;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.dto.sector.SectorPlataformaDTO;
import com.antecsis.dto.sector.SectorRequestDTO;
import com.antecsis.dto.sector.SectorResponseDTO;
import com.antecsis.entity.Sector;
import com.antecsis.entity.Usuario;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.SectorRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.service.SectorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SectorServiceImpl implements SectorService {

    private final SectorRepository repository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public SectorResponseDTO crear(SectorRequestDTO dto) {
        Sector s = new Sector();
        s.setNombreSector(dto.nombreSector());
        s.setTelefono(dto.telefono());
        s.setDireccion(dto.direccion());
        s.setVideoPromocionalUrl(normalizeUrl(dto.videoPromocionalUrl()));
        Sector guardado = repository.save(s);
        return toDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SectorResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public SectorResponseDTO obtenerPorId(Long id) {
        Sector s = repository.findById(id).orElseThrow(() -> new BusinessException("Sector no existe"));
        return toDTO(s);
    }

    @Override
    @Transactional
    public SectorResponseDTO actualizar(Long id, SectorRequestDTO dto) {
        Sector s = repository.findById(id).orElseThrow(() -> new BusinessException("Sector no existe"));
        s.setNombreSector(dto.nombreSector());
        s.setTelefono(dto.telefono());
        s.setDireccion(dto.direccion());
        s.setVideoPromocionalUrl(normalizeUrl(dto.videoPromocionalUrl()));
        return toDTO(repository.save(s));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectorPlataformaDTO> listarParaPlataforma() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario u = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        String rol = u.getRol() != null ? u.getRol().getNombre() : "";
        if ("SUPERUSUARIO".equals(rol)) {
            return repository.findAll().stream()
                    .sorted(Comparator.comparing(Sector::getNombreSector, String.CASE_INSENSITIVE_ORDER))
                    .map(this::toPlataforma)
                    .toList();
        }
        if (u.getSede() != null) {
            return List.of(toPlataforma(u.getSede()));
        }
        return List.of();
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!repository.existsById(id)) throw new BusinessException("Sector no existe");
        if (usuarioRepository.existsBySede_Id(id)) {
            throw new BusinessException("No se puede eliminar el sector porque tiene usuarios asignados. Reasigne o elimine los usuarios primero.");
        }
        repository.deleteById(id);
    }

    private SectorResponseDTO toDTO(Sector s) {
        return new SectorResponseDTO(
                s.getId(),
                s.getNombreSector(),
                s.getTelefono(),
                s.getDireccion(),
                s.getVideoPromocionalUrl()
        );
    }

    private SectorPlataformaDTO toPlataforma(Sector s) {
        return new SectorPlataformaDTO(
                s.getId(),
                s.getNombreSector(),
                s.getTelefono(),
                s.getDireccion(),
                s.getVideoPromocionalUrl()
        );
    }

    private static String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String t = url.trim();
        return t.length() > 500 ? t.substring(0, 500) : t;
    }
}
