package com.example.climbingnav.auth.controller;

import com.example.climbingnav.global.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
public class TokenController {
    private final JwtUtil jwtUtil;

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "REFRESH", required = false) String refresh,
                                     HttpServletResponse resp) {
        if (refresh == null || !jwtUtil.validateRefresh(refresh)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "INVALID_REFRESH"));
        }

        String userId = jwtUtil.getSubject(refresh);
        String accessToken = jwtUtil.createAccess(userId, Map.of()); // 15~60분

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "tokenType", "Bearer",
                "expireIn", 3600
        ));
    }
}
