package com.example.climbingnav.auth.controller;

import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.service.RefreshTokenService;
import com.example.climbingnav.auth.service.UserService;
import com.example.climbingnav.global.base.ApiResponse;
import com.example.climbingnav.global.base.types.ResponseCode;
import com.example.climbingnav.global.exception.CustomException;
import com.example.climbingnav.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
public class TokenController {
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "REFRESH", required = false) String refresh) {
        if (refresh == null || !jwtUtil.validateRefresh(refresh)) {
            throw new CustomException(ResponseCode.UNAUTHORIZED, "refresh 토큰 값이 유효 하지 않습니다. 다시 로그인해주세요.");
        }

        String userId = jwtUtil.getSubject(refresh);

        if (!refreshTokenService.validateRefreshToken(Long.valueOf(userId), refresh)) {
            throw new CustomException(ResponseCode.UNAUTHORIZED, "토큰이 유효하지 않습니다.");
        }

        User user = userService.getUserProfile(userId);

        String accessToken = jwtUtil.createAccess(userId, Map.of(
                "email", user.getEmail(),
                "nickname", user.getNickname()));

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "tokenType", "Bearer",
                "expireIn", 3600
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal(expression = "principal") String userId) {
        refreshTokenService.deleteRefreshToken(Long.valueOf(userId));

        return ResponseEntity.ok().body(ApiResponse.ok("logout success!"));
    }
}
