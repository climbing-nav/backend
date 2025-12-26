package com.example.climbingnav.auth.controller;

import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.service.RefreshTokenService;
import com.example.climbingnav.auth.service.UserService;
import com.example.climbingnav.global.base.ApiResponse;
import com.example.climbingnav.global.base.UserVo;
import com.example.climbingnav.global.base.types.ResponseCode;
import com.example.climbingnav.global.exception.CustomException;
import com.example.climbingnav.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
    public ResponseEntity<?> refresh(
            @CookieValue(value = "REFRESH", required = false) String refresh
    ) {
        if (refresh == null || refresh.isBlank()) {
            throw new CustomException(ResponseCode.UNAUTHORIZED, "refresh 토큰이 없습니다. 다시 로그인해주세요.");
        }

        if (!jwtUtil.validateRefresh(refresh)) {
            throw new CustomException(ResponseCode.UNAUTHORIZED, "refresh 토큰 값이 유효하지 않습니다. 다시 로그인해주세요.");
        }

        Long userId = Long.valueOf(jwtUtil.getSubject(refresh));

        if (!refreshTokenService.validateRefreshToken(userId, refresh)) {
            throw new CustomException(ResponseCode.UNAUTHORIZED, "토큰이 유효하지 않습니다.");
        }

        User user = userService.getUserProfile(String.valueOf(userId));

        String accessToken = jwtUtil.createAccess(String.valueOf(userId), Map.of(
                "email", user.getEmail(),
                "nickname", user.getNickname()
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        return ResponseEntity.ok()
                .headers(headers).build();
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal UserVo userVo) {
        refreshTokenService.deleteRefreshToken(userVo.userId());

        return ResponseEntity.ok().body(ApiResponse.ok("logout success!"));
    }
}
