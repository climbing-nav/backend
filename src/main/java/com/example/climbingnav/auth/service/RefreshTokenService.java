package com.example.climbingnav.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.jwt.refresh-seconds}")
    private long refreshExpSeconds;

    public void saveRefreshToken(Long userId, String refreshToken) {
        String key = buildKey(userId);
        stringRedisTemplate.opsForValue()
                .set(key, refreshToken, Duration.ofSeconds(refreshExpSeconds));
    }

    public boolean validateRefreshToken(Long userId, String refreshToken) {
        String key = buildKey(userId);
        String savedToken = stringRedisTemplate.opsForValue().get(key);
        if (savedToken == null) return false;
        return savedToken.equals(refreshToken);
    }

    public void deleteRefreshToken(Long userId) {
        String key = buildKey(userId);
        stringRedisTemplate.delete(key);
    }

    private String buildKey(Long userId) {
        return "RT:" + userId;
    }
}
