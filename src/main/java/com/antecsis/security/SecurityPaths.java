package com.antecsis.security;

import java.util.List;

/**
 * Rutas públicas que no requieren autenticación JWT.
 * Centralizadas para mantener coherencia entre SecurityConfig y documentación.
 */
public final class SecurityPaths {

    private SecurityPaths() {}

    /** Endpoint de login (único que recibe credenciales). */
    public static final String LOGIN = "/api/auth/login";

    /** Endpoint de renovación de token con refresh token. */
    public static final String REFRESH = "/api/auth/refresh";

    /** WebSocket. */
    public static final String WS = "/ws/**";

    /** Documentación Swagger / OpenAPI. */
    public static final List<String> SWAGGER = List.of(
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs",
            "/api-docs/**",
            "/v3/api-docs/**"
    );

    /** Rutas que permiten acceso anónimo (sin token). */
    public static final String[] PUBLIC = new String[] {
            LOGIN,
            REFRESH,
            WS,
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs",
            "/api-docs/**",
            "/v3/api-docs/**"
    };
}
