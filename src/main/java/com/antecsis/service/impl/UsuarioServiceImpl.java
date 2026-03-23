package com.antecsis.service.impl;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.dto.usuario.UsuarioCorreoDTO;
import com.antecsis.dto.usuario.UsuarioCreateRequest;
import com.antecsis.dto.usuario.UsuarioResponseDTO;
import com.antecsis.dto.usuario.UsuarioUpdateRequest;
import com.antecsis.entity.Modulo;
import com.antecsis.entity.Rol;
import com.antecsis.entity.Sector;
import com.antecsis.entity.Usuario;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.ModuloRepository;
import com.antecsis.repository.PasswordResetTokenRepository;
import com.antecsis.repository.RefreshTokenRepository;
import com.antecsis.repository.RolRepository;
import com.antecsis.repository.SectorRepository;
import com.antecsis.repository.SolicitudRecuperacionRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.service.UsuarioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    /** Documento: Usuario Secundario -> Cajero, Ventas, Logística, Administración. */
    private static final Set<String> ROLES_PERMITIDOS = Set.of("ADMIN", "CAJERO", "ALMACENERO", "VENTAS", "LOGISTICA", "ADMINISTRACION", "SOPORTE");

    private static final int MAX_CAJEROS_POR_LICENCIA = 3;
    private static final int MAX_VENTAS_POR_LICENCIA = 1;

    private static final Map<String, Set<String>> MODULOS_POR_DEFECTO = Map.of(
        "ADMIN", Set.of("DASHBOARD", "VENTAS", "COMPRAS", "PRODUCTOS", "INVENTARIO",
                "CLIENTES", "PROVEEDORES", "REPORTES", "SOLICITUDES_STOCK",
                "SOLICITUDES_PRODUCTO", "CATEGORIAS", "METODOS_PAGO",
                "HISTORIAL_PEDIDOS", "MENSAJES", "USUARIOS", "LOGISTICA_ENTREGAS"),
        "CAJERO", Set.of("DASHBOARD", "VENTAS", "CLIENTES", "METODOS_PAGO"),
        "ALMACENERO", Set.of("DASHBOARD", "INVENTARIO", "PRODUCTOS", "CATEGORIAS",
                "SOLICITUDES_STOCK"),
        "VENTAS", Set.of("DASHBOARD", "VENTAS", "CLIENTES", "PRODUCTOS",
                "REPORTES", "METODOS_PAGO", "LOGISTICA_ENTREGAS"),
        "LOGISTICA", Set.of("DASHBOARD", "INVENTARIO", "COMPRAS", "PROVEEDORES",
                "SOLICITUDES_STOCK", "SOLICITUDES_PRODUCTO", "HISTORIAL_PEDIDOS", "LOGISTICA_ENTREGAS"),
        "ADMINISTRACION", Set.of("DASHBOARD", "REPORTES", "VENTAS", "COMPRAS",
                "CLIENTES", "PROVEEDORES", "MENSAJES"),
        "SOPORTE", Set.of("DASHBOARD", "CLIENTES", "PRODUCTOS", "REPORTES", "MENSAJES", "VENTAS")
    );

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SectorRepository sectorRepository;
    private final ModuloRepository moduloRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final SolicitudRecuperacionRepository solicitudRecuperacionRepository;

    @Override
    @Transactional
    public void crearUsuario(UsuarioCreateRequest dto) {
        Usuario principal = validarYObtenerUsuarioPrincipal();

        if (!usuarioRepository.findByUsername(dto.username()).isEmpty()) {
            throw new BusinessException("El usuario ya existe");
        }

        if (!ROLES_PERMITIDOS.contains(dto.rol())) {
            throw new BusinessException("Rol no permitido. Use: ADMIN, CAJERO, ALMACENERO, VENTAS, LOGISTICA, ADMINISTRACION o SOPORTE");
        }
        if (esAdmin(principal) && "ADMIN".equals(dto.rol())) {
            throw new BusinessException("Solo puede crear usuarios de su punto (Cajero, Almacenero, Ventas, etc.). No puede crear otro Administrador.");
        }
        if ("ADMIN".equals(dto.rol()) && dto.sedeId() != null) {
            long adminsEnSede = usuarioRepository.countBySede_IdAndRol_Nombre(dto.sedeId(), "ADMIN");
            if (adminsEnSede >= 1) {
                throw new BusinessException("Solo puede existir un Administrador por sede. Esta sede ya tiene uno asignado.");
            }
        }

        Rol rol = rolRepository.findByNombre(dto.rol())
                .orElseThrow(() -> new BusinessException("Rol no válido: " + dto.rol()));

        // Reglas de licencias: Max 3 Cajeros, Max 1 Ventas por usuario principal (documento)
        Long principalId = principal.getId();
        if ("CAJERO".equals(dto.rol())) {
            long count = usuarioRepository.countByUsuarioPrincipalIdAndRolNombre(principalId, "CAJERO");
            if (count >= MAX_CAJEROS_POR_LICENCIA) {
                throw new BusinessException("Límite de Cajeros alcanzado (" + MAX_CAJEROS_POR_LICENCIA + "). Se requiere licencia adicional.");
            }
        } else if ("VENTAS".equals(dto.rol())) {
            long count = usuarioRepository.countByUsuarioPrincipalIdAndRolNombre(principalId, "VENTAS");
            if (count >= MAX_VENTAS_POR_LICENCIA) {
                throw new BusinessException("Límite de usuarios Ventas alcanzado (" + MAX_VENTAS_POR_LICENCIA + "). Se requiere licencia adicional.");
            }
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.username());
        usuario.setPassword(passwordEncoder.encode(dto.password()));
        usuario.setNombre(dto.nombre());
        usuario.setApellido(dto.apellido());
        usuario.setCorreo(dto.correo());
        usuario.setFechaNacimiento(dto.fechaNacimiento());
        if (esSuperusuario(principal)) {
            if (dto.sedeId() == null) {
                throw new BusinessException("La sede es obligatoria para todos los usuarios.");
            }
            Sector sede = sectorRepository.findById(dto.sedeId())
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

        List<Modulo> modulosPorDefecto;
        if ("ADMIN".equals(dto.rol())) {
            modulosPorDefecto = moduloRepository.findByActivoTrueOrderByOrdenAsc();
        } else {
            Set<String> codigosPorDefecto = MODULOS_POR_DEFECTO.getOrDefault(dto.rol(), Set.of("DASHBOARD"));
            modulosPorDefecto = moduloRepository.findByCodigoInAndActivoTrue(codigosPorDefecto);
        }
        usuario.setModulos(new HashSet<>(modulosPorDefecto));

        usuarioRepository.save(usuario);
        log.info("Usuario creado por {}: {} con rol {} y {} módulos",
                principal.getUsername(), dto.username(), dto.rol(), modulosPorDefecto.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listar(Pageable pageable) {
        Usuario actual = obtenerUsuarioActual();
        if (esSuperusuario(actual)) {
            // SUPERUSUARIO solo ve los ADMINs (uno por bodega): son sus "clientes"
            return usuarioRepository.findByRol_Nombre("ADMIN", pageable).map(this::toResponseDTO);
        }
        if (esAdmin(actual) && actual.getSede() != null) {
            // ADMIN ve solo los usuarios de su propia bodega (excluyéndose a sí mismo)
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
        if (dto.nombre() != null) u.setNombre(dto.nombre());
        if (dto.apellido() != null) u.setApellido(dto.apellido());
        if (dto.correo() != null) u.setCorreo(dto.correo());
        if (dto.fechaNacimiento() != null) u.setFechaNacimiento(dto.fechaNacimiento());
        if (dto.rol() != null && !dto.rol().isBlank()) {
            Rol rol = rolRepository.findByNombre(dto.rol().trim())
                    .orElseThrow(() -> new BusinessException("Rol no válido: " + dto.rol()));
            u.setRol(rol);
        }
        Usuario actual = obtenerUsuarioActual();
        if (esSuperusuario(actual)) {
            if (dto.sedeId() != null) {
                Sector sede = sectorRepository.findById(dto.sedeId())
                        .orElseThrow(() -> new BusinessException("Sede no encontrada"));
                u.setSede(sede);
            }
        }
        // ADMIN no puede cambiar la sede del usuario (solo SUPERUSUARIO)
        if (dto.activo() != null) u.setActivo(dto.activo());
        if (dto.puedeRecuperarContrasena() != null) u.setPuedeRecuperarContrasena(dto.puedeRecuperarContrasena());
        if (dto.password() != null && !dto.password().isBlank()) {
            u.setPassword(passwordEncoder.encode(dto.password()));
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
    public void actualizarPuedeRecuperarContrasena(Long id, boolean puedeRecuperarContrasena) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        validarAccesoAdminOSoporte(u);
        u.setPuedeRecuperarContrasena(puedeRecuperarContrasena);
        usuarioRepository.save(u);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        validarAccesoSede(u);
        Long usuarioId = u.getId();
        refreshTokenRepository.deleteByUsuarioId(usuarioId);
        passwordResetTokenRepository.deleteByUsuarioId(usuarioId);
        solicitudRecuperacionRepository.deleteByUsuario_Id(usuarioId);
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

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioCorreoDTO> buscarPorCorreo(String q) {
        if (q == null || q.trim().length() < 2) {
            return List.of();
        }
        return usuarioRepository.findTop10ByCorreoContainingIgnoreCaseOrderByCorreo(q.trim()).stream()
                .filter(u -> u.getCorreo() != null && !u.getCorreo().isBlank())
                .map(u -> new UsuarioCorreoDTO(
                        u.getCorreo(),
                        (Objects.requireNonNullElse(u.getNombre(), "") + " " + Objects.requireNonNullElse(u.getApellido(), "")).trim()
                ))
                .toList();
    }

    private UsuarioResponseDTO toResponseDTO(Usuario u) {
        Long sedeId = u.getSede() != null ? u.getSede().getId() : null;
        String sedeNombre = u.getSede() != null ? u.getSede().getNombreSector() : null;
        String rolNombre = u.getRol() != null ? u.getRol().getNombre() : null;
        Integer edad = null;
        if (u.getFechaNacimiento() != null) {
            edad = Period.between(u.getFechaNacimiento(), LocalDate.now()).getYears();
        }
        Set<String> modulos = u.getModulos() != null
                ? u.getModulos().stream().map(Modulo::getCodigo).collect(Collectors.toSet())
                : Set.of();
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
                u.getActivo(),
                modulos,
                Boolean.TRUE.equals(u.getPuedeRecuperarContrasena())
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

    /** ADMIN/SOPORTE/SUPERUSUARIO pueden cambiar puedeRecuperarContrasena de usuarios de su sector. */
    private void validarAccesoAdminOSoporte(Usuario target) {
        Usuario actual = obtenerUsuarioActual();
        if (target.getId().equals(actual.getId())) {
            throw new BusinessException("No puede cambiar su propio permiso de recuperación");
        }
        String rol = actual.getRol() != null ? actual.getRol().getNombre() : null;
        if ("SUPERUSUARIO".equals(rol)) return;
        if ("ADMIN".equals(rol) || "SOPORTE".equals(rol)) {
            if (actual.getSede() == null) throw new BusinessException("No tiene sede asignada");
            if (target.getSede() == null || !target.getSede().getId().equals(actual.getSede().getId()))
                throw new BusinessException("Solo puede gestionar usuarios de su sector");
            return;
        }
        throw new BusinessException("Sin permiso para esta operación");
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
