package com.antecsis.dto.permiso;

import java.util.Set;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermisoUpdateRequest {
    @NotNull
    private Set<String> moduloCodigos;
}
