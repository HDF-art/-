package com.agri.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class JwtUtils {
    
    private static final String SECRET_KEY = "AgriPlatformSecretKey2026VeryLongSecureKey123456";
    private static final long EXPIRATION = 24 * 60 * 60 * 1000L;
    
    public static String generateToken(String username, Long userId, String role) {
        String uniqueId = System.currentTimeMillis() + "-" + new Random().nextInt(1000000);
        
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .claim("uniqueId", uniqueId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
    
    public static Map<String, Object> validateToken(String token) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
            
            result.put("valid", true);
            result.put("username", claims.getSubject());
            result.put("userId", claims.get("userId"));
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
    
    public static String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
    
    public static Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
