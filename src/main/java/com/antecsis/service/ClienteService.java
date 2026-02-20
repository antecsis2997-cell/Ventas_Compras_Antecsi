package com.antecsis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.antecsis.dto.cliente.ClienteRequestDTO;
import com.antecsis.dto.cliente.ClienteResponseDTO;

public interface ClienteService {
    ClienteResponseDTO crear(ClienteRequestDTO dto);
    Page<ClienteResponseDTO> listar(Pageable pageable, String search);
    ClienteResponseDTO obtenerPorId(Long id);
    ClienteResponseDTO actualizar(Long id, ClienteRequestDTO dto);
    void eliminar(Long id);
}
