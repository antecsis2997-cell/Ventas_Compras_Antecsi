package com.antecsis.service.impl;

import com.antecsis.dto.sector.SectorRequestDTO;
import com.antecsis.dto.sector.SectorResponseDTO;
import com.antecsis.entity.Sector;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.SectorRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.service.SectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SectorServiceImpl implements SectorService {

    private final SectorRepository repository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public SectorResponseDTO crear(SectorRequestDTO dto) {
        Sector s = new Sector();
        s.setNombreSector(dto.getNombreSector());
        s.setTelefono(dto.getTelefono());
        s.setDireccion(dto.getDireccion());
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
        s.setNombreSector(dto.getNombreSector());
        s.setTelefono(dto.getTelefono());
        s.setDireccion(dto.getDireccion());
        return toDTO(repository.save(s));
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
        return new SectorResponseDTO(s.getId(), s.getNombreSector(), s.getTelefono(), s.getDireccion());
    }
}
