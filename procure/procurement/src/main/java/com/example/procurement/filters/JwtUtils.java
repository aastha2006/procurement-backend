package com.example.procurement.filters;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.procurement.entity.AppUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret; // Base64-encoded string

    @Value("${jwt.expirationMs}")
    private int jwtExpirationMs;

    @Value("${security.jwt.refrehexpiration-time}")
    private long refreshTokenExpiration;

    // Generate access + refresh tokens
    public Map<String, String> generateJwtToken(AppUser userDetails) {
        String accessToken = createAccessToken(userDetails);
        String refreshToken = createRefreshToken(userDetails);
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);
        tokens.put("refresh_token", refreshToken);
        tokens.put("refreshExpirytime", String.valueOf(System.currentTimeMillis() + refreshTokenExpiration));
        return tokens;
    }

    // Create access token with claims
    private String createAccessToken(AppUser userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .toList())
                .claim("email", userDetails.getEmail())
                .claim("id", userDetails.getId())
                .claim("group", userDetails.getIdGroup() != null ? userDetails.getIdGroup().getId() : null)
                .claim("groupName", userDetails.getIdGroup() != null ? userDetails.getIdGroup().getName() : null)
                .claim("Enable", userDetails.getEnabled())
                .claim("loginType", userDetails.getLoginType())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Create refresh token
    private String createRefreshToken(AppUser userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Extract any claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Extract roles claim
    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> claims.get("roles", List.class));
    }

    // Decode base64 key and return signing key
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
