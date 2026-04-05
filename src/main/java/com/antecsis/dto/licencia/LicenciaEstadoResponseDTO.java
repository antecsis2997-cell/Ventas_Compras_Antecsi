package com.antecsis.dto.licencia;

import java.time.LocalDate;

public record LicenciaEstadoResponseDTO(
        boolean tieneContexto,
        String planEtiqueta,
        String planCodigo,
        String estado,
        LocalDate vigenciaHasta,
        boolean licenciaActivada,
        String rubroNombre,
        String mensaje
) {}
