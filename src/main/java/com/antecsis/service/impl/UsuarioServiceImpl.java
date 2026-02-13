package com.antecsis.service.impl;

import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.dto.usuario.UsuarioCreateRequest;
import com.antecsis.entity.Rol;
import com.antecsis.entity.Sector;
import com.antecsis.entity.Usuario;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.RolRepository;
import com.antecsis.repository.SectorRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.service.UsuarioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    /** Documento: Usuario Secundario -> Cajero, Ventas, Logística, Administración. */
    private static final Set<String> ROLES_PERMITIDOS = Set.of("ADMIN", "CAJERO", "ALMACENERO", "VENTAS", "LOGISTICA", "ADMINISTRACION");

    private static final int MAX_CAJEROS_POR_LICENCIA = 3;
    private static final int MAX_VENTAS_POR_LICENCIA = 1;

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SectorRepository sectorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void crearUsuario(UsuarioCreateRequest dto) {
        Usuario principal = validarYObtenerUsuarioPrincipal();

        if (usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new BusinessException("El usuario ya existe");
        }

        if (!ROLES_PERMITIDOS.contains(dto.getRol())) {
            throw new BusinessException("Rol no permitido. Use: ADMIN, CAJERO, ALMACENERO, VENTAS, LOGISTICA o ADMINISTRACION");
        }

        Rol rol = rolRepository.findByNombre(dto.getRol())
                .orElseThrow(() -> new BusinessException("Rol no válido: " + dto.getRol()));

        // Reglas de licencias: Max 3 Cajeros, Max 1 Ventas por usuario principal (documento)
        Long principalId = principal.getId();
        if ("CAJERO".equals(dto.getRol())) {
            long count = usuarioRepository.countByUsuarioPrincipalIdAndRolNombre(principalId, "CAJERO");
            if (count >= MAX_CAJEROS_POR_LICENCIA) {
                throw new BusinessException("Límite de Cajeros alcanzado (" + MAX_CAJEROS_POR_LICENCIA + "). Se requiere licencia adicional.");
            }
        } else if ("VENTAS".equals(dto.getRol())) {
            long count = usuarioRepository.countByUsuarioPrincipalIdAndRolNombre(principalId, "VENTAS");
            if (count >= MAX_VENTAS_POR_LICENCIA) {
                throw new BusinessException("Límite de usuarios Ventas alcanzado (" + MAX_VENTAS_POR_LICENCIA + "). Se requiere licencia adicional.");
            }
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setCorreo(dto.getCorreo());
        usuario.setEdad(dto.getEdad());
        usuario.setCargo(dto.getCargo());
        if (dto.getSedeId() != null) {
            Sector sede = sectorRepository.findById(dto.getSedeId())
                    .orElse(null);
            usuario.setSede(sede);
        }
        usuario.setRol(rol);
        usuario.setUsuarioPrincipalId(principalId);
        usuario.setActivo(true);

        usuarioRepository.save(usuario);
        log.info("Usuario creado por {}: {} con rol {}", principal.getUsername(), dto.getUsername(), dto.getRol());
    }

    /** Solo SUPERUSUARIO o ADMIN pueden crear usuarios secundarios (documento). */
    private Usuario validarYObtenerUsuarioPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new BusinessException("No autenticado");
        }
        String username = auth.getName();
        Usuario principal = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        boolean puedeCrear = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPERUSUARIO".equals(a.getAuthority()) || "ROLE_ADMIN".equals(a.getAuthority()));
        if (!puedeCrear) {
            throw new BusinessException("Solo el superusuario o administrador puede crear usuarios");
        }
        return principal;
    }
}
