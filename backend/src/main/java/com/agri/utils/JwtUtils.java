package com.agri.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.*;
import java.security.SecureRandom;

@Component
public class JwtUtils {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration:86400000}")
    private long expiration;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    public String generateToken(String username, Long userId, String role) {
        String uniqueId = System.currentTimeMillis() + "-" + secureRandom.nextInt(1000000);
        
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .claim("uniqueId", uniqueId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
    
    public Map<String, Object> validateToken(String token) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            
            result.put("valid", true);
            result.put("username", claims.getSubject());
            Object userIdObj = claims.get("userId");
            if (userIdObj instanceof Integer) {
                result.put("userId", ((Integer) userIdObj).longValue());
            } else if (userIdObj instanceof Long) {
                result.put("userId", userIdObj);
            } else {
                result.put("userId", Long.parseLong(userIdObj.toString()));
            }
            result.put("role", claims.get("role"));
            
        } catch (ExpiredJwtException e) {
            result.put("valid", false);
            result.put("message", "Token已过期");
        } catch (JwtException e) {
            result.put("valid", false);
            result.put("message", "Token无效");
        }
        
        return result;
    }
    
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
    
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    public String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
