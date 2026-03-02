package com.antecsis.service.impl;

import com.antecsis.dto.cliente.ClienteRequestDTO;
import com.antecsis.dto.cliente.ClienteResponseDTO;
import com.antecsis.entity.Cliente;
import com.antecsis.entity.Sector;
import com.antecsis.entity.Usuario;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.ClienteRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.service.ClienteService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public ClienteResponseDTO crear(ClienteRequestDTO dto) {
        Usuario usuario = obtenerUsuarioAutenticado();
        Long sectorId = usuario.getSede() != null ? usuario.getSede().getId() : null;

        // Validar email duplicado por sector
        if (sectorId != null) {
            repository.findByEmailAndSectorId(dto.getEmail(), sectorId).ifPresent(c -> {
                throw new BusinessException("El email ya está registrado en tu bodega");
            });
        } else {
            repository.findByEmail(dto.getEmail()).ifPresent(c -> {
                throw new BusinessException("El email ya está registrado");
            });
        }

        // Validar documento duplicado por sector
        if (sectorId != null && dto.getDocumento() != null && !dto.getDocumento().isBlank()) {
            if (repository.existsByDocumentoAndSectorId(dto.getDocumento(), sectorId)) {
                throw new BusinessException("El documento ya está registrado en tu bodega");
            }
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
        cliente.setTipoDocumento(dto.getTipoDocumento());
        cliente.setDocumento(dto.getDocumento());
        cliente.setDireccion(dto.getDireccion());
        cliente.setSector(usuario.getSede());
        cliente.setActivo(true);

        Cliente guardado = repository.save(cliente);
        return toResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponseDTO> listar(Pageable pageable, String search) {
        Long sectorId = obtenerSectorIdAutenticado();

        if (search != null && !search.trim().isEmpty()) {
            if (sectorId != null) {
                return repository.buscarPorNombreODocumentoYSector(search.trim(), sectorId, pageable).map(this::toResponseDTO);
            }
            return repository.buscarPorNombreODocumento(search.trim(), pageable).map(this::toResponseDTO);
        }

        if (sectorId != null) {
            return repository.findBySectorId(sectorId, pageable).map(this::toResponseDTO);
        }
        return repository.findAll(pageable).map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerPorId(Long id) {
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente no existe"));
        verificarAccesoSector(cliente.getSector());
        return toResponseDTO(cliente);
    }

    @Override
    @Transactional
    public ClienteResponseDTO actualizar(Long id, ClienteRequestDTO dto) {
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente no existe"));
        verificarAccesoSector(cliente.getSector());

        Long sectorId = obtenerSectorIdAutenticado();
        if (sectorId != null) {
            repository.findByEmailAndSectorId(dto.getEmail(), sectorId).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new BusinessException("El email ya está registrado por otro cliente en tu bodega");
                }
            });
            // Validar documento duplicado
            if (dto.getDocumento() != null && !dto.getDocumento().isBlank()) {
                boolean existeOtroConMismoDocumento = repository.findBySectorId(sectorId, Pageable.unpaged())
                    .stream()
                    .anyMatch(c -> !c.getId().equals(id) && dto.getDocumento().equals(c.getDocumento()));
                if (existeOtroConMismoDocumento) {
                    throw new BusinessException("El documento ya está registrado por otro cliente en tu bodega");
                }
            }
        } else {
            repository.findByEmail(dto.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new BusinessException("El email ya está registrado por otro cliente");
                }
            });
        }

        cliente.setNombre(dto.getNombre());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
        cliente.setTipoDocumento(dto.getTipoDocumento());
        cliente.setDocumento(dto.getDocumento());
        cliente.setDireccion(dto.getDireccion());

        Cliente guardado = repository.save(cliente);
        return toResponseDTO(guardado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente no existe"));
        verificarAccesoSector(cliente.getSector());
        cliente.setActivo(false);
        repository.save(cliente);
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

    private ClienteResponseDTO toResponseDTO(Cliente c) {
        return new ClienteResponseDTO(
                c.getId(),
                c.getNombre(),
                c.getEmail(),
                c.getTelefono(),
                c.getTipoDocumento(),
                c.getDocumento(),
                c.getDireccion(),
                c.getActivo()
        );
    }
}
