package com.antecsis.service.impl;

import com.antecsis.dto.cliente.ClienteRequestDTO;
import com.antecsis.dto.cliente.ClienteResponseDTO;
import com.antecsis.entity.Cliente;
import com.antecsis.repository.ClienteRepository;
import com.antecsis.service.ClienteService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;

    public ClienteServiceImpl(ClienteRepository repository) {
        this.repository = repository;
    }

    @Override
    public ClienteResponseDTO crear(ClienteRequestDTO dto) {
        repository.findByEmail(dto.getEmail()).ifPresent(c -> {
            throw new RuntimeException("El email ya está registrado");
        });

        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());

        Cliente guardado = repository.save(cliente);

        return new ClienteResponseDTO(
                guardado.getId(),
                guardado.getNombre(),
                guardado.getEmail(),
                guardado.getTelefono()
        );
    }

    @Override
    public List<ClienteResponseDTO> listar() {
        return repository.findAll()
                .stream()
                .map(c -> new ClienteResponseDTO(
                        c.getId(),
                        c.getNombre(),
                        c.getEmail(),
                        c.getTelefono()
                ))
                .toList();
    }
}
