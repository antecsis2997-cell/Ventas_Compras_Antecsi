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

    /** Olvidé contraseña y restablecer contraseña. */
    public static final String FORGOT_PASSWORD = "/api/auth/forgot-password";
    public static final String RESET_PASSWORD = "/api/auth/reset-password";
    public static final String PUEDE_RECUPERAR = "/api/auth/puede-recuperar";

    /** Compra pública de plan (obtener el programa). */
    public static final String COMPRA_PUBLICA = "/api/suscripciones/compra-publica";

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
            FORGOT_PASSWORD,
            RESET_PASSWORD,
            PUEDE_RECUPERAR,
            COMPRA_PUBLICA,
            WS,
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs",
            "/api-docs/**",
            "/v3/api-docs/**"
    };
}
