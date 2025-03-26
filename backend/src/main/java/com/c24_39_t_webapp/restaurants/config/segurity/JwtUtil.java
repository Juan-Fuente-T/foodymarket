package com.c24_39_t_webapp.restaurants.config.segurity;

import com.c24_39_t_webapp.restaurants.models.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private long expiration;
    public String generateToken(String email, String role){
        return Jwts.builder()
                .subject(email)
                .claims(Map.of("email", email,"role", role))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(getSigningKey())
                .compact();
    }
    private SecretKey getSigningKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }
    public String extractRole(String token) { return getClaims(token).get("role", String.class);}

    public boolean validateToken(String token){
        try{
            getClaims(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }
    public boolean isValidToken(String token, UserEntity user){
        String email = extractEmail(token);
        return (email.equals(user.getEmail())) && !isExpiredToken(token);
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
