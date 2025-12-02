package com.example.climbingnav.global.jwt;

import com.example.climbingnav.global.base.UserVo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {
    private final Key key;
    private final long accessSeconds;   // 3600
    private final long refreshSeconds;  // 2592000 (30d)

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.access-seconds}") long accessSeconds,
                   @Value("${app.jwt.refresh-seconds}") long refreshSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessSeconds = accessSeconds;
        this.refreshSeconds = refreshSeconds;
    }

    public String createAccess(String subject, Map<String,Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefresh(String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshSeconds)))
                .claim("typ", "refresh")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateRefresh(String jwtStr) {
        try {
            var c = parse(jwtStr);
            return "refresh".equals(c.get("typ"));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }

    public String getSubject(String token) {
        return parse(token).getSubject();
    }

    public boolean validateToken(String jwtToken) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwtToken);

            return !claimsJws.getBody().getExpiration().before(new Date());
        } catch (JwtException e) {
            log.info("Invalid JWT Token", e);
            return false;
        }
    }
}
