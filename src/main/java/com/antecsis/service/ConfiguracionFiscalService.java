package com.antecsis.service;

import com.antecsis.dto.sunat.ConfiguracionFiscalRequestDTO;
import com.antecsis.dto.sunat.ConfiguracionFiscalResponseDTO;
import com.antecsis.entity.ConfiguracionFiscal;

import java.util.List;
import java.util.Optional;

public interface ConfiguracionFiscalService {

    /**
     * SUPERUSUARIO → devuelve todas las configuraciones.
     * ADMIN        → devuelve solo la de su propio sector.
     */
    List<ConfiguracionFiscalResponseDTO> listarParaUsuario(String username);

    /**
     * SUPERUSUARIO → puede guardar para cualquier sector.
     * ADMIN        → solo puede guardar para su propio sector.
     */
    ConfiguracionFiscalResponseDTO guardarParaUsuario(ConfiguracionFiscalRequestDTO dto, String username);

    /**
     * SUPERUSUARIO → puede activar cualquiera.
     * ADMIN        → solo la suya.
     */
    ConfiguracionFiscalResponseDTO activarParaUsuario(Long id, String username);

    /**
     * SUPERUSUARIO → puede desactivar cualquiera.
     * ADMIN        → solo la suya.
     */
    ConfiguracionFiscalResponseDTO desactivarParaUsuario(Long id, String username);

    /** Para uso interno (servicios SUNAT): devuelve entidad completa con credenciales. */
    Optional<ConfiguracionFiscal> buscarActivaPorSector(Long sectorId);
}
