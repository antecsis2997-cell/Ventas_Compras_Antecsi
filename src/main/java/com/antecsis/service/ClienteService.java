
package com.antecsis.service;

import com.antecsis.dto.cliente.ClienteRequestDTO;
import com.antecsis.dto.cliente.ClienteResponseDTO;

import java.util.List;

public interface ClienteService {
    ClienteResponseDTO crear(ClienteRequestDTO dto);
    List<ClienteResponseDTO> listar();
}
