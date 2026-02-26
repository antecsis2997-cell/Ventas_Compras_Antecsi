package com.antecsis.service;

import java.util.List;
import java.util.Set;

import com.antecsis.dto.permiso.ModuloDTO;

public interface PermisoService {

    List<ModuloDTO> listarModulos();

    List<ModuloDTO> obtenerPermisosUsuario(Long usuarioId);

    void actualizarPermisosUsuario(Long usuarioId, Set<String> moduloCodigos);
}
