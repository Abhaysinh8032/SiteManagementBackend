package com.abhay.management.site.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT utility for jjwt 0.13.x
 *
 * Key API changes from 0.11.x → 0.13.x:
 *  - Jwts.parserBuilder()  →  Jwts.parser()
 *  - .setSigningKey()      →  .verifyWith()
 *  - Keys.hmacShaKeyFor()  still works but returns SecretKey, not Key
 *  - SignatureAlgorithm enum removed → use Jwts.SIG.HS256
 *  - .signWith(key, algo)  →  .signWith(key, Jwts.SIG.HS256)  OR  just .signWith(key)
 */
@Component
@Slf4j
public class JwtUtil {
	
	private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {

        // Secret must be ≥ 32 ASCII chars (256 bits) for HS256
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // ── Generate ──────────────────────────────────────────────────────────────

    /**
     * Create a signed JWT containing employeeId (subject), role, and userId claims.
     */
    public String generateToken(String employeeId, String role, String userId) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(employeeId)            // 0.13.x: .subject() not .setSubject()
                .claim("role", role)
                .claim("userId", userId)
                .issuedAt(now)                  // 0.13.x: .issuedAt() not .setIssuedAt()
                .expiration(expiry)             // 0.13.x: .expiration() not .setExpiration()
                .signWith(signingKey)           // 0.13.x: algorithm inferred from key type
                .compact();
    }

    // ── Validate ──────────────────────────────────────────────────────────────

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT malformed: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT is empty or null: {}", e.getMessage());
        }
        return false;
    }

    // ── Extract claims ────────────────────────────────────────────────────────

    public String getEmployeeId(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public String getUserId(String token) {
        return parseClaims(token).get("userId", String.class);
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parser()               // 0.13.x: .parser() not .parserBuilder()
                .verifyWith(signingKey)    // 0.13.x: .verifyWith() not .setSigningKey()
                .build()
                .parseSignedClaims(token) // 0.13.x: .parseSignedClaims() not .parseClaimsJws()
                .getPayload();            // 0.13.x: .getPayload() not .getBody()
    }
}