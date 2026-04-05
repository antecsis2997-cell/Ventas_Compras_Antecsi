package com.antecsis.service;

import com.antecsis.dto.suscripcion.SuscripcionRequestDTO;
import com.antecsis.dto.suscripcion.SuscripcionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SuscripcionService {
    SuscripcionResponseDTO crear(SuscripcionRequestDTO dto);
    Page<SuscripcionResponseDTO> listar(Pageable pageable, String estado, Long rubroId);
    SuscripcionResponseDTO obtenerPorId(Long id);
    SuscripcionResponseDTO actualizar(Long id, SuscripcionRequestDTO dto);
    void eliminar(Long id);
    void enviarAlerta(Long id);
    void ejecutarAlertasAutomaticas();

    /** Aviso por correo cuando la suscripción vence en los próximos N días (licencia próxima a caducar). */
    void ejecutarAlertasProximoVencimiento();
    void compraPublica(String plan, String ruc, String nombreCliente, String correoAdministrador, String rubroCodigo,
                       String nombreTitularTarjeta, String numeroTarjeta, String fechaCaducidadTarjeta, Long sectorId);
}
