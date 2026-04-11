package com.antecsis.security;

/**
 * Nombres de rol persistidos en {@code roles.nombre} y usados en JWT (ROLE_*).
 * <ul>
 *   <li>{@link #SUPERADMIN} — dueño de la plataforma (AnTecsis).</li>
 *   <li>{@link #SUPERUSUARIO} — cliente con una o más bodegas licenciadas; administra solo esas sedes.</li>
 * </ul>
 */
public final class RolNombre {

    public static final String SUPERADMIN = "SUPERADMIN";
    public static final String SUPERUSUARIO = "SUPERUSUARIO";
    public static final String ADMIN = "ADMIN";

    private RolNombre() {}
}
