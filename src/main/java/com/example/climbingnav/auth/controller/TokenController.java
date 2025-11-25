package com.example.climbingnav.auth.controller;

import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.service.UserService;
import com.example.climbingnav.global.base.types.ResponseCode;
import com.example.climbingnav.global.exception.CustomException;
import com.example.climbingnav.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
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
    private final UserService userService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "REFRESH", required = false) String refresh) {
        if (refresh == null || !jwtUtil.validateRefresh(refresh)) {
            throw new CustomException(ResponseCode.UNAUTHORIZED, "refresh 토큰 값이 유효 하지 않습니다. 다시 로그인해주세요.");
        }

        String userId = jwtUtil.getSubject(refresh);

        User user = userService.getUserProfile(userId);

        if(user == null) {
            throw new CustomException(ResponseCode.UNAUTHORIZED, "회원가입된 계정이 없습니다.");
        }

        String accessToken = jwtUtil.createAccess(userId, Map.of(
                "email", user.getEmail(),
                "nickname", user.getNickname()));

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "tokenType", "Bearer",
                "expireIn", 3600
        ));
    }
}
