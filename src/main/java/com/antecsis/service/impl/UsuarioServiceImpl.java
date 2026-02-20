package com.antecsis.service.impl;

import java.time.LocalDate;
import java.time.Period;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.dto.usuario.UsuarioCreateRequest;
import com.antecsis.dto.usuario.UsuarioResponseDTO;
import com.antecsis.dto.usuario.UsuarioUpdateRequest;
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
        if (esAdmin(principal) && "ADMIN".equals(dto.getRol())) {
            throw new BusinessException("Solo puede crear usuarios de su punto (Cajero, Almacenero, Ventas, etc.). No puede crear otro Administrador.");
        }
        if ("ADMIN".equals(dto.getRol()) && dto.getSedeId() != null) {
            long adminsEnSede = usuarioRepository.countBySede_IdAndRol_Nombre(dto.getSedeId(), "ADMIN");
            if (adminsEnSede >= 1) {
                throw new BusinessException("Solo puede existir un Administrador por sede. Esta sede ya tiene uno asignado.");
            }
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
        usuario.setFechaNacimiento(dto.getFechaNacimiento());
        if (esSuperusuario(principal)) {
            if (dto.getSedeId() == null) {
                throw new BusinessException("La sede es obligatoria para todos los usuarios.");
            }
            Sector sede = sectorRepository.findById(dto.getSedeId())
                    .orElseThrow(() -> new BusinessException("Sede no encontrada"));
            usuario.setSede(sede);
        } else {
            if (principal.getSede() == null) {
                throw new BusinessException("Debe tener una sede asignada para crear usuarios.");
            }
            usuario.setSede(principal.getSede());
        }
        usuario.setRol(rol);
        usuario.setUsuarioPrincipalId(principalId);
        usuario.setActivo(true);

        usuarioRepository.save(usuario);
        log.info("Usuario creado por {}: {} con rol {}", principal.getUsername(), dto.getUsername(), dto.getRol());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listar(Pageable pageable) {
        Usuario actual = obtenerUsuarioActual();
        if (esSuperusuario(actual)) {
            return usuarioRepository.findAll(pageable).map(this::toResponseDTO);
        }
        if (esAdmin(actual) && actual.getSede() != null) {
            return usuarioRepository.findBySede_Id(actual.getSede().getId(), pageable).map(this::toResponseDTO);
        }
        return Page.empty(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        validarAccesoSede(u);
        return toResponseDTO(u);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO actualizar(Long id, UsuarioUpdateRequest dto) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        validarAccesoSede(u);
        boolean eraAdmin = u.getRol() != null && "ADMIN".equals(u.getRol().getNombre());
        Long sedeAnteriorId = u.getSede() != null ? u.getSede().getId() : null;
        if (dto.getNombre() != null) u.setNombre(dto.getNombre());
        if (dto.getApellido() != null) u.setApellido(dto.getApellido());
        if (dto.getCorreo() != null) u.setCorreo(dto.getCorreo());
        if (dto.getFechaNacimiento() != null) u.setFechaNacimiento(dto.getFechaNacimiento());
        if (dto.getRol() != null && !dto.getRol().isBlank()) {
            Rol rol = rolRepository.findByNombre(dto.getRol().trim())
                    .orElseThrow(() -> new BusinessException("Rol no válido: " + dto.getRol()));
            u.setRol(rol);
        }
        Usuario actual = obtenerUsuarioActual();
        if (esSuperusuario(actual)) {
            if (dto.getSedeId() != null) {
                Sector sede = sectorRepository.findById(dto.getSedeId())
                        .orElseThrow(() -> new BusinessException("Sede no encontrada"));
                u.setSede(sede);
            }
        }
        // ADMIN no puede cambiar la sede del usuario (solo SUPERUSUARIO)
        if (dto.getActivo() != null) u.setActivo(dto.getActivo());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            u.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (u.getSede() == null) {
            throw new BusinessException("La sede es obligatoria para todos los usuarios.");
        }
        if ("ADMIN".equals(u.getRol().getNombre()) && u.getSede() != null) {
            Long sedeIdFinal = u.getSede().getId();
            long adminsEnSede = usuarioRepository.countBySede_IdAndRol_Nombre(sedeIdFinal, "ADMIN");
            boolean eraAdminEnEstaSede = eraAdmin && sedeAnteriorId != null && sedeAnteriorId.equals(sedeIdFinal);
            if (adminsEnSede >= 1 && !eraAdminEnEstaSede) {
                throw new BusinessException("Solo puede existir un Administrador por sede. Esta sede ya tiene uno asignado.");
            }
        }
        return toResponseDTO(usuarioRepository.save(u));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        validarAccesoSede(u);
        usuarioRepository.delete(u);
    }

    @Override
    @Transactional
    public void actualizarActivo(Long id, boolean activo) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        validarAccesoSede(u);
        u.setActivo(activo);
        usuarioRepository.save(u);
    }

    private UsuarioResponseDTO toResponseDTO(Usuario u) {
        Long sedeId = u.getSede() != null ? u.getSede().getId() : null;
        String sedeNombre = u.getSede() != null ? u.getSede().getNombreSector() : null;
        String rolNombre = u.getRol() != null ? u.getRol().getNombre() : null;
        Integer edad = null;
        if (u.getFechaNacimiento() != null) {
            edad = Period.between(u.getFechaNacimiento(), LocalDate.now()).getYears();
        }
        return new UsuarioResponseDTO(
                u.getId(),
                u.getUsername(),
                u.getNombre(),
                u.getApellido(),
                u.getCorreo(),
                edad,
                u.getFechaNacimiento(),
                sedeId,
                sedeNombre,
                rolNombre,
                u.getActivo()
        );
    }

    /** SUPERUSUARIO = dueño de la solución. ADMIN = admin de su sede (punto/bodega); solo controla usuarios de su sede. */
    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new BusinessException("No autenticado");
        return usuarioRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
    }

    private boolean esSuperusuario(Usuario u) {
        return u != null && u.getRol() != null && "SUPERUSUARIO".equals(u.getRol().getNombre());
    }

    private boolean esAdmin(Usuario u) {
        return u != null && u.getRol() != null && "ADMIN".equals(u.getRol().getNombre());
    }

    /** Nadie puede editar/desactivar/eliminar su propio usuario. ADMIN solo puede tocar usuarios de su misma sede. */
    private void validarAccesoSede(Usuario target) {
        Usuario actual = obtenerUsuarioActual();
        if (target.getId().equals(actual.getId())) {
            throw new BusinessException("No puede editar, desactivar o eliminar su propio usuario");
        }
        if (esSuperusuario(actual)) return;
        if (esAdmin(actual)) {
            if (actual.getSede() == null)
                throw new BusinessException("No tiene sede asignada");
            if (target.getSede() == null || !target.getSede().getId().equals(actual.getSede().getId()))
                throw new BusinessException("Solo puede gestionar usuarios de su sede");
            return;
        }
        throw new BusinessException("Sin permiso para esta operación");
    }

    /** Solo SUPERUSUARIO o ADMIN pueden crear usuarios secundarios (documento). */
    private Usuario validarYObtenerUsuarioPrincipal() {
        Usuario principal = obtenerUsuarioActual();
        if (!esSuperusuario(principal) && !esAdmin(principal)) {
            throw new BusinessException("Solo el superusuario o administrador puede crear usuarios");
        }
        return principal;
    }
}
