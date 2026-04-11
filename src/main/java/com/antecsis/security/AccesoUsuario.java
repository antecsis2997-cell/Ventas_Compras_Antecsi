package com.antecsis.security;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.antecsis.entity.Sector;
import com.antecsis.entity.Usuario;

/**
 * Reglas de acceso por sede sin acoplar a un servicio concreto.
 */
public final class AccesoUsuario {

    /**
     * Dueño de plataforma: rol SUPERADMIN en BD, o SUPERUSUARIO sin bodegas gestionadas
     * (cuenta plataforma antes de migrar el nombre del rol a SUPERADMIN).
     */
    public static boolean esSuperadmin(Usuario u) {
        if (u == null || u.getRol() == null) {
            return false;
        }
        String nombre = u.getRol().getNombre();
        if (RolNombre.SUPERADMIN.equals(nombre)) {
            return true;
        }
        return RolNombre.SUPERUSUARIO.equals(nombre) && idsSectoresGestionados(u).isEmpty();
    }

    /** Superusuario cliente: licencia multi-bodega (al menos una bodega en sectores gestionados). */
    public static boolean esSuperusuarioCliente(Usuario u) {
        return u != null && u.getRol() != null && RolNombre.SUPERUSUARIO.equals(u.getRol().getNombre())
                && !idsSectoresGestionados(u).isEmpty();
    }

    public static boolean esAdmin(Usuario u) {
        return u != null && u.getRol() != null && RolNombre.ADMIN.equals(u.getRol().getNombre());
    }

    public static Set<Long> idsSectoresGestionados(Usuario u) {
        if (u == null || u.getSectoresGestionados() == null || u.getSectoresGestionados().isEmpty()) {
            return Set.of();
        }
        return u.getSectoresGestionados().stream()
                .map(Sector::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /** Puede operar sobre la sede indicada (activa o como dueño multi-bodega). */
    public static boolean puedeGestionarSede(Usuario u, Long sectorId) {
        if (sectorId == null) {
            return false;
        }
        if (esSuperadmin(u)) {
            return true;
        }
        if (esSuperusuarioCliente(u)) {
            return idsSectoresGestionados(u).contains(sectorId);
        }
        if (esAdmin(u) && u.getSede() != null) {
            return sectorId.equals(u.getSede().getId());
        }
        return false;
    }

    /** Igual que {@link #puedeGestionarSede} pero incluye usuarios cuya sede está en el conjunto gestionado. */
    public static boolean puedeGestionarUsuarioPorSede(Usuario actor, Usuario target) {
        if (target == null || target.getSede() == null) {
            return false;
        }
        return puedeGestionarSede(actor, target.getSede().getId());
    }

    public static Set<Long> idsSectoresParaListar(Usuario u) {
        if (esSuperadmin(u)) {
            return null; // null = sin filtro (todas)
        }
        if (esSuperusuarioCliente(u)) {
            Set<Long> ids = idsSectoresGestionados(u);
            return ids.isEmpty() ? Collections.emptySet() : ids;
        }
        if (esAdmin(u) && u.getSede() != null) {
            return Set.of(u.getSede().getId());
        }
        if (u.getSede() != null) {
            return Set.of(u.getSede().getId());
        }
        return Collections.emptySet();
    }

    private AccesoUsuario() {}
}
