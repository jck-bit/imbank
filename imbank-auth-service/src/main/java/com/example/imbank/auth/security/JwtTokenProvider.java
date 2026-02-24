package com.example.imbank.auth.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

   //convert tghe secret string to cryptograpvic key fir signing
    private SecretKey getSigningKey() {
        // Convert secret string to bytes
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //generate a jwt access for authenticated usr

    public String generateAccessToken(UserDetails userDetails) {

        // 1. Extract roles from UserDetails
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 2. Create custom claims (additional data in JWT)
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);

        // 3. Calculate expiration time
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        // 4. Build JWT
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer(jwtIssuer)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Parse JWT token and extract claims
     *
     * @param token JWT token string
     * @return Claims object containing all token data
     * @throws JwtException if token is invalid
     */

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract username from JWT token
     *
     * @param token JWT token string
     * @return username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();  // Subject = username
    }

    /**
     * Validate JWT token
     * Checks signature, expiration, and format
     *
     * @param token JWT token string
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            // Try to parse the token - if successful, it's valid
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            return false;

        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            return false;

        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token format: {}", e.getMessage());
            return false;

        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            return false;

        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            return false;

        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract roles from JWT token
     *
     * @param token JWT token string
     * @return comma-separated roles string
     */
    public String getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("roles", String.class);
        // Returns: "ROLE_USER,ROLE_ADMIN"
    }
}
