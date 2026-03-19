package com.antecsis.service;

import com.antecsis.dto.conversion.ConversionRequestDTO;
import com.antecsis.dto.conversion.ConversionResponseDTO;

public interface ConversionService {
    ConversionResponseDTO convertir(ConversionRequestDTO dto);
}

