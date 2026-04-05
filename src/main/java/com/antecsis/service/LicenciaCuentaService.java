package com.antecsis.service;

import com.antecsis.dto.licencia.ActivarLicenciaRequestDTO;
import com.antecsis.dto.licencia.LicenciaEstadoResponseDTO;

public interface LicenciaCuentaService {
    LicenciaEstadoResponseDTO estadoMiCuenta();

    void activar(ActivarLicenciaRequestDTO dto);
}
