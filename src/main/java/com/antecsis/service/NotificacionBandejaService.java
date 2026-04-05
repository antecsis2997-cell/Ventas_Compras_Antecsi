package com.antecsis.service;

import java.util.List;

import com.antecsis.dto.notificacion.NotificacionBandejaDTO;

public interface NotificacionBandejaService {
    void registrar(String emailDestino, String tipo, String titulo, String cuerpoResumen);

    List<NotificacionBandejaDTO> listarParaUsuarioAutenticado();
}
