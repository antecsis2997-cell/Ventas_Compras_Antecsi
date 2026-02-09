package com.antecsis.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {
	private static final String SECRET = "CLAVE_SUPER_SECRETA_123456789_ANTECSIS";
    private static final long EXPIRATION = 1000 * 60 * 60; // 1 hora

    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static String generarToken(String username, String rol) {
        return Jwts.builder()
                .setSubject(username)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims validarToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
