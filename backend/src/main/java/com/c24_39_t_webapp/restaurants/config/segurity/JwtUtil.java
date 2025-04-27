package com.c24_39_t_webapp.restaurants.config.segurity;

import com.c24_39_t_webapp.restaurants.models.UserEntity;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;


@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String email, String role, Long id){
        SecretKey key = getSigningKey(); // Obtenemos la clave para HS256
        return Jwts.builder()
                // Cabecera mínima (solo alg:HS256, sin typ)
                .setHeaderParam("alg", SignatureAlgorithm.HS256.getValue())
                .subject(email) // 'sub': Va email porque NO tenemos UUID de SupabaseAuth en este flujo. Si lo hubiera iría aquí.
                .audience().add("authenticated").and() // 'aud': Estándar para Supabase RLS
                // --> ¡Quitamos temporalmente los claims personalizados! <--
                // .claims(Map.of("email", email, "role", role, "id", id))
                // .claim("app_metadata", Map.of("id", id, "userrole", role))
                // --> ¡Añadimos el rol estándar! <--
                .claim("role", "authenticated") // <-- Claim 'role' estándar que usa Supabase RLS
                // Claims de tiempo estándar
                .issuedAt(new Date(System.currentTimeMillis())) // 'iat'
                .expiration(new Date(System.currentTimeMillis() + expiration)) // 'exp'
                // Firma con la clave correcta y HS256
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    private SecretKey getSigningKey(){
//        byte[] keyBytes = Decoders.BASE64.decode(secret);
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
//        return Keys.hmacShaKeyFor(keyBytes);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
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
    public void testSelfVerification() {
        System.out.println("--- Iniciando Auto-Verificación JWT ---");
        try {
            // 1. Obtener la clave (usa el mismo método que para firmar)
            SecretKey key = getSigningKey();
            if (key == null) {
                System.err.println("Self-Test - ERROR: getSigningKey() devolvió null!");
                return;
            }
            System.out.println("Self-Test - Clave obtenida.");

            // 2. Generar un token de prueba simple
            String testToken = Jwts.builder()
                    .subject("self-test-user")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 60000)) // Expira en 1 min
                    .signWith(key, SignatureAlgorithm.HS512) // Usa MISMA clave y algoritmo
                    .compact();
            System.out.println("Self-Test - Token Generado: " + testToken);

            // 3. Intentar verificar ESE MISMO token con ESA MISMA clave
            JwtParser parser = Jwts.parser()
                    .verifyWith(key) // Usa la misma clave para verificar
                    .build();

            Jws<Claims> claimsJws = parser.parseSignedClaims(testToken); // Esto lanzará excepción si falla

            // Si llegamos aquí, la verificación fue exitosa
            System.out.println("Self-Test - ¡¡VERIFICACIÓN EXITOSA!! Subject: " + claimsJws.getPayload().getSubject());

        } catch (Exception e) {
            // Si la verificación falla, se captura aquí
            System.err.println("Self-Test - ¡¡VERIFICACIÓN FALLIDA!! Error: " + e.getMessage());
            e.printStackTrace(); // Imprime detalles del error
        }
        System.out.println("--- Fin Auto-Verificación JWT ---");
    }
}
