package com.c24_39_t_webapp.restaurants.config.security;

import com.c24_39_t_webapp.restaurants.models.UserEntity;
import io.jsonwebtoken.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
//import javax.crypto.spec.SecretKeySpec;
//import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String email, String userRole, Long userId){
        SecretKey key = getSigningKey(); // Obtenemos la clave para HS256
        return Jwts.builder()
                .setHeaderParam("alg", SignatureAlgorithm.HS256.getValue())
                .subject(email) // 'sub': Va email porque NO hay UUID de SupabaseAuth en este flujo. Si lo hubiera iría aquí.
                // .claim("app_metadata", Map.of("id", userId, "role", userRole))
                 .claim("role", userRole)
                 .claim("id", userId)
                // Claims de tiempo estándar
                .issuedAt(new Date(System.currentTimeMillis())) // 'iat'
                .expiration(new Date(System.currentTimeMillis() + expiration)) // 'exp'
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    private SecretKey getSigningKey(){
        //Es válida para claves en Base64
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
        /* Usa UTF-8. NO USAR. → No valida seguridad → Causa errores silenciosos → No evita claves débiles. Usar la siguiente.
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA256");*/
        // Usa Keys.hmacShaKeyFor para crear la clave directamente desde los bytes UTF-8. Evita problemas de longitud mínima.
//        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }
    public String extractRole(String token) { return getClaims(token).get("role", String.class);}
    public String extractId(String token) {return String.valueOf(getClaims(token).get("id", Long.class));}
    public boolean validateToken(String token){
        try{
            getClaims(token);
            return true;
        }catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    public boolean isValidToken(String token, UserEntity user){
        try {
            if (!validateToken(token)) return false;

            String email = extractEmail(token);
            return email.equals(user.getEmail()) && !isExpiredToken(token);

        } catch (Exception e) {
            return false;  // Devuelve false si hay cualquier excepción
        }
    }
    public boolean isExpiredToken(String token){
        return getClaims(token).getExpiration().before(new Date());
    }
    private Claims getClaims(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token).
                getPayload();
    }
}
