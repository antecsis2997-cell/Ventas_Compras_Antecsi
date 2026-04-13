package com.antecsis.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.dto.sector.SectorActivoRequestDTO;
import com.antecsis.dto.sector.SectorPlataformaDTO;
import com.antecsis.dto.sector.SectorRequestDTO;
import com.antecsis.dto.sector.SectorResponseDTO;
import com.antecsis.entity.Sector;
import com.antecsis.entity.Usuario;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.SectorRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.security.AccesoUsuario;
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
        s.setActivo(true);
        Sector guardado = repository.save(s);
        return toDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SectorResponseDTO> listar(Pageable pageable) {
        Usuario u = usuarioActual();
        if (AccesoUsuario.esSuperadmin(u)) {
            return repository.findAll(pageable).map(this::toDTO);
        }
        if (AccesoUsuario.esSuperusuarioCliente(u)) {
            var ids = AccesoUsuario.idsSectoresGestionados(u);
            if (ids.isEmpty()) {
                return Page.<SectorResponseDTO>empty(pageable);
            }
            return repository.findByIdInAndActivoTrue(ids, pageable).map(this::toDTO);
        }
        if (u.getSede() != null) {
            Optional<Sector> una = repository.findById(u.getSede().getId());
            if (una.isPresent()) {
                return new PageImpl<>(List.of(toDTO(una.get())), pageable, 1);
            }
            return Page.<SectorResponseDTO>empty(pageable);
        }
        return Page.<SectorResponseDTO>empty(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public SectorResponseDTO obtenerPorId(Long id) {
        Sector s = repository.findById(id).orElseThrow(() -> new BusinessException("Sector no existe"));
        validarAccesoLectura(s.getId());
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
    @Transactional
    public SectorResponseDTO cambiarActivo(Long id, SectorActivoRequestDTO dto) {
        Sector s = repository.findById(id).orElseThrow(() -> new BusinessException("Sector no existe"));
        s.setActivo(Boolean.TRUE.equals(dto.activo()));
        return toDTO(repository.save(s));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectorPlataformaDTO> listarParaPlataforma() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario u = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        if (AccesoUsuario.esSuperadmin(u)) {
            return repository.findAll().stream()
                    .filter(Sector::isActivo)
                    .sorted(Comparator.comparing(Sector::getNombreSector, String.CASE_INSENSITIVE_ORDER))
                    .map(this::toPlataforma)
                    .toList();
        }
        if (AccesoUsuario.esSuperusuarioCliente(u)) {
            return u.getSectoresGestionados().stream()
                    .filter(Sector::isActivo)
                    .sorted(Comparator.comparing(Sector::getNombreSector, String.CASE_INSENSITIVE_ORDER))
                    .map(this::toPlataforma)
                    .toList();
        }
        if (u.getSede() != null) {
            Sector sede = u.getSede();
            if (!sede.isActivo()) {
                return List.of();
            }
            return List.of(toPlataforma(sede));
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

    private Usuario usuarioActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
    }

    private void validarAccesoLectura(Long sectorId) {
        Usuario u = usuarioActual();
        if (AccesoUsuario.esSuperadmin(u)) {
            return;
        }
        if (!AccesoUsuario.puedeGestionarSede(u, sectorId)) {
            throw new BusinessException("No tiene acceso a este sector");
        }
    }

    private SectorResponseDTO toDTO(Sector s) {
        return new SectorResponseDTO(
                s.getId(),
                s.getNombreSector(),
                s.getTelefono(),
                s.getDireccion(),
                s.getVideoPromocionalUrl(),
                s.isActivo()
        );
    }

    private SectorPlataformaDTO toPlataforma(Sector s) {
        return new SectorPlataformaDTO(
                s.getId(),
                s.getNombreSector(),
                s.getTelefono(),
                s.getDireccion(),
                s.getVideoPromocionalUrl(),
                s.isActivo()
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
