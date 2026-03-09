package com.antecsis.dto.categoria;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequestDTO(
    @NotBlank
    String nombre
) {}
