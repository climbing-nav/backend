package com.example.climbingnav.global.jwt;

import com.example.climbingnav.auth.config.KakaoProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@RequiredArgsConstructor
public class JwtUtil {
    private final SecretKey key;
    private final long expSeconds;

    public JwtUtil(KakaoProperties props) {
        byte[] keyBytes = Decoders.BASE64.decode(java.util.Base64.getEncoder().encodeToString(props.getJwtSecret().getBytes()));
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expSeconds = props.getJwtExpSeconds();
    }

    public String createToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(subject)
                .setClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
