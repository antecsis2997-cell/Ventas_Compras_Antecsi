package com.antecsis.dto.notificacion;

import java.time.LocalDateTime;

public record NotificacionBandejaDTO(
        Long id,
        String tipo,
        String titulo,
        String cuerpoResumen,
        LocalDateTime createdAt
) {}
