package com.antecsis.dto.permiso;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record PermisoUpdateRequest(
    @NotNull
    Set<String> moduloCodigos
) {}
