package com.antecsis.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.dto.permiso.ModuloDTO;
import com.antecsis.entity.Modulo;
import com.antecsis.entity.Usuario;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.ModuloRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.service.PermisoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermisoServiceImpl implements PermisoService {

    private final ModuloRepository moduloRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ModuloDTO> listarModulos() {
        return moduloRepository.findByActivoTrueOrderByOrdenAsc()
                .stream()
                .map(m -> new ModuloDTO(m.getId(), m.getCodigo(), m.getNombre(),
                        m.getDescripcion(), m.getIcono(), m.getOrden(), false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuloDTO> obtenerPermisosUsuario(Long usuarioId) {
        Usuario actual = obtenerUsuarioActual();
        Usuario target = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        validarAcceso(actual, target);

        Set<String> codigosAsignados = target.getModulos().stream()
                .map(Modulo::getCodigo)
                .collect(Collectors.toSet());

        return moduloRepository.findByActivoTrueOrderByOrdenAsc()
                .stream()
                .map(m -> new ModuloDTO(m.getId(), m.getCodigo(), m.getNombre(),
                        m.getDescripcion(), m.getIcono(), m.getOrden(),
                        codigosAsignados.contains(m.getCodigo())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void actualizarPermisosUsuario(Long usuarioId, Set<String> moduloCodigos) {
        Usuario actual = obtenerUsuarioActual();
        Usuario target = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        validarAcceso(actual, target);

        if (target.getId().equals(actual.getId())) {
            throw new BusinessException("No puede modificar sus propios permisos");
        }

        Set<String> codigosPermitidos = actual.getModulos().stream()
                .map(Modulo::getCodigo)
                .collect(Collectors.toSet());

        if (esSuperusuario(actual)) {
            codigosPermitidos = moduloRepository.findByActivoTrueOrderByOrdenAsc()
                    .stream().map(Modulo::getCodigo).collect(Collectors.toSet());
        }

        for (String codigo : moduloCodigos) {
            if (!codigosPermitidos.contains(codigo)) {
                throw new BusinessException("No puede asignar el módulo '" + codigo
                        + "' porque usted mismo no tiene acceso a él");
            }
        }

        List<Modulo> modulos = moduloRepository.findByCodigoInAndActivoTrue(moduloCodigos);
        target.setModulos(new HashSet<>(modulos));
        usuarioRepository.save(target);

        log.info("Permisos actualizados para usuario {} por {}: {}",
                target.getUsername(), actual.getUsername(), moduloCodigos);
    }

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new BusinessException("No autenticado");
        return usuarioRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
    }

    private boolean esSuperusuario(Usuario u) {
        return u.getRol() != null && "SUPERUSUARIO".equals(u.getRol().getNombre());
    }

    private boolean esAdmin(Usuario u) {
        return u.getRol() != null && "ADMIN".equals(u.getRol().getNombre());
    }

    private void validarAcceso(Usuario actual, Usuario target) {
        if (esSuperusuario(actual)) return;
        if (esAdmin(actual)) {
            if (actual.getSede() == null)
                throw new BusinessException("No tiene sede asignada");
            if (target.getSede() == null || !target.getSede().getId().equals(actual.getSede().getId()))
                throw new BusinessException("Solo puede gestionar permisos de usuarios de su sede");
            return;
        }
        throw new BusinessException("Sin permiso para gestionar permisos de otros usuarios");
    }
}
