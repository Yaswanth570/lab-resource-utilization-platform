package com.labresource.platform.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class JwtServiceImpl implements JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtServiceImpl.class);

    private final SecretKey signingKey;
    private final long jwtExpirationMs;

    public JwtServiceImpl(@Value("${jwt.secret:}") String jwtSecret,
                          @Value("${jwt.expiration-ms:86400000}") long jwtExpirationMs) {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT signing secret is missing. The JWT_SECRET environment variable must be configured.");
        }
        this.signingKey = initSigningKey(jwtSecret.trim());
        this.jwtExpirationMs = jwtExpirationMs;
    }

    private SecretKey initSigningKey(String secret) {
        byte[] keyBytes = null;
        try {
            byte[] decoded = Decoders.BASE64.decode(secret);
            if (decoded.length >= 32) {
                keyBytes = decoded;
            }
        } catch (Exception ignored) {
        }
        if (keyBytes == null) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT signing secret is invalid: must provide at least 256 bits (32 bytes) of key material.");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateToken(Long userId, String email, Long institutionId, List<String> roles) {
        return generateToken(userId, email, institutionId, null, roles);
    }

    @Override
    public String generateToken(Long userId, String email, Long institutionId, Long departmentId, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        var builder = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("institutionId", institutionId)
                .claim("roles", roles != null ? roles : Collections.emptyList())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey);

        if (departmentId != null) {
            builder.claim("departmentId", departmentId);
        }

        return builder.compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT signature or malformed token");
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty or invalid");
        }
        return false;
    }

    @Override
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    @Override
    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email", String.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return Collections.emptyList();
    }

    @Override
    public Long extractInstitutionId(String token) {
        Claims claims = extractAllClaims(token);
        Object instObj = claims.get("institutionId");
        if (instObj instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    @Override
    public Long extractDepartmentId(String token) {
        Claims claims = extractAllClaims(token);
        Object deptObj = claims.get("departmentId");
        if (deptObj instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public long getExpirationMs() {
        return jwtExpirationMs;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
