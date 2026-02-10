package com.antecsis.service.impl;

import com.antecsis.dto.cliente.ClienteRequestDTO;
import com.antecsis.dto.cliente.ClienteResponseDTO;
import com.antecsis.entity.Cliente;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.ClienteRepository;
import com.antecsis.service.ClienteService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;

    @Override
    @Transactional
    public ClienteResponseDTO crear(ClienteRequestDTO dto) {
        repository.findByEmail(dto.getEmail()).ifPresent(c -> {
            throw new BusinessException("El email ya está registrado");
        });

        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
        cliente.setTipoDocumento(dto.getTipoDocumento());
        cliente.setDocumento(dto.getDocumento());
        cliente.setDireccion(dto.getDireccion());
        cliente.setActivo(true);

        Cliente guardado = repository.save(cliente);
        return toResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerPorId(Long id) {
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente no existe"));
        return toResponseDTO(cliente);
    }

    @Override
    @Transactional
    public ClienteResponseDTO actualizar(Long id, ClienteRequestDTO dto) {
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente no existe"));

        repository.findByEmail(dto.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessException("El email ya está registrado por otro cliente");
            }
        });

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
        cliente.setActivo(false);
        repository.save(cliente);
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
