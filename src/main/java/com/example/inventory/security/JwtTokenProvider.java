package com.example.inventory.security;

import com.example.inventory.exception.InvalidTokenException;
import com.example.inventory.exception.TokenExpiredException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * Generate JWT Token from Authentication
     */
    public String generateTokenFromAuthentication(Authentication authentication) {
        return generateToken(authentication.getName(), authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
    }

    /**
     * Generate JWT Token with username and roles
     */
    public String generateToken(String username, java.util.List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Generate JWT Token with username only
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Get signing key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get username from JWT token
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * Get all claims from token - ✅ FIXED
     */
    public Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()  // ✅ Correct method
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            logger.error("JWT token has expired: {}", ex.getMessage());
            throw new TokenExpiredException("JWT token has expired");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
            throw new InvalidTokenException("Unsupported JWT token");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
            throw new InvalidTokenException("Invalid JWT token");
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
            throw new InvalidTokenException("Invalid JWT signature");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
            throw new InvalidTokenException("JWT claims string is empty");
        }
    }

    /**
     * Get expiration date from token
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * Check if token is expired
     */
    private Boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (TokenExpiredException ex) {
            return true;
        }
    }

    /**
     * Validate JWT token - ✅ FIXED
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()  // ✅ Correct method
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (ExpiredJwtException ex) {
            logger.error("JWT token has expired");
            throw new TokenExpiredException("JWT token has expired");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
            throw new InvalidTokenException("Unsupported JWT token");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
            throw new InvalidTokenException("Invalid JWT token");
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
            throw new InvalidTokenException("Invalid JWT signature");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty");
            throw new InvalidTokenException("JWT claims string is empty");
        }
    }
}