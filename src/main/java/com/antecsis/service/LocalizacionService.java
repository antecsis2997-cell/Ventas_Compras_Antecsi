package com.antecsis.service;

import com.antecsis.dto.localizacion.LocalizacionRequestDTO;
import com.antecsis.dto.localizacion.LocalizacionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LocalizacionService {
    LocalizacionResponseDTO crear(LocalizacionRequestDTO dto);
    Page<LocalizacionResponseDTO> listar(Pageable pageable);
    LocalizacionResponseDTO obtenerPorId(Long id);
    LocalizacionResponseDTO actualizar(Long id, LocalizacionRequestDTO dto);
    void eliminar(Long id);
}
