package com.cefet.liftly.security;

import com.cefet.liftly.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/** Assinatura e validação dos JWTs, seguindo a divisão do projeto JWT - Parte II. */
@Service
public class JwtService {
    private final SecretKey chave;
    private final long expiracaoMs;

    public JwtService(@Value("${JWT_SECRET}") String secret,
                      @Value("${jwt.expiration:86400000}") long expiracaoMs) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET deve ter pelo menos 32 bytes");
        }
        if (expiracaoMs <= 0) {
            throw new IllegalArgumentException("jwt.expiration deve ser positivo");
        }
        // Compatível com a chave de texto fornecida via variável JWT_SECRET no Render.
        this.chave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMs = expiracaoMs;
    }

    public String gerarToken(Usuario usuario) {
        Date agora = new Date();
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("perfil", usuario.getPerfil())
                .issuedAt(agora)
                .expiration(new Date(agora.getTime() + expiracaoMs))
                .signWith(chave)
                .compact();
    }

    public String getLogin(String token) {
        return getClaims(token).getSubject();
    }

    public boolean validarToken(String token) {
        try {
            String login = getLogin(token);
            return login != null && !login.isBlank();
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
