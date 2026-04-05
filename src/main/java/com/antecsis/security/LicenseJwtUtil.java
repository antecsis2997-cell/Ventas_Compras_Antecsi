package com.antecsis.security;

import java.security.Key;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class LicenseJwtUtil {

    private static final int MIN_SECRET_LENGTH = 32;
    public static final String CLAIM_TYP = "typ";
    public static final String LICENSE_TYP = "LICENSE";
    public static final String CLAIM_PLAN = "plan";

    private final Key key;

    public LicenseJwtUtil(@Value("${license.jwt.secret}") String secret) {
        if (secret == null || secret.getBytes().length < MIN_SECRET_LENGTH) {
            throw new IllegalArgumentException(
                    "license.jwt.secret debe tener al menos " + MIN_SECRET_LENGTH + " caracteres");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generarLicencia(long suscripcionId, String planCodigo, String jti, LocalDate vigenciaHasta) {
        Date exp = Date.from(vigenciaHasta.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .setSubject(String.valueOf(suscripcionId))
                .setId(jti)
                .claim(CLAIM_TYP, LICENSE_TYP)
                .claim(CLAIM_PLAN, planCodigo != null ? planCodigo : "")
                .setIssuedAt(new Date())
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseAndValidate(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token.trim())
                .getBody();
        if (!LICENSE_TYP.equals(claims.get(CLAIM_TYP))) {
            throw new io.jsonwebtoken.JwtException("Token no es una licencia de plan");
        }
        return claims;
    }
}
