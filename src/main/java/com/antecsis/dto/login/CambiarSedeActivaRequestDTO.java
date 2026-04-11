package com.antecsis.dto.login;

import jakarta.validation.constraints.NotNull;

public record CambiarSedeActivaRequestDTO(
    @NotNull(message = "sectorId es obligatorio")
    Long sectorId
) {}
