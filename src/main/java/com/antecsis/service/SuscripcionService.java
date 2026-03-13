package com.antecsis.service;

import com.antecsis.dto.suscripcion.SuscripcionRequestDTO;
import com.antecsis.dto.suscripcion.SuscripcionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SuscripcionService {
    SuscripcionResponseDTO crear(SuscripcionRequestDTO dto);
    Page<SuscripcionResponseDTO> listar(Pageable pageable, String estado);
    SuscripcionResponseDTO obtenerPorId(Long id);
    SuscripcionResponseDTO actualizar(Long id, SuscripcionRequestDTO dto);
    void eliminar(Long id);
    void enviarAlerta(Long id);
    void ejecutarAlertasAutomaticas();
    void compraPublica(String plan, String ruc, String nombreCliente, String nombreTitularTarjeta,
                       String numeroTarjeta, String fechaCaducidadTarjeta, Long sectorId);
}
