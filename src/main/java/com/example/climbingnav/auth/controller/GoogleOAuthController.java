package com.example.climbingnav.auth.controller;

import com.example.climbingnav.auth.dto.GoogleTokenResponse;
import com.example.climbingnav.auth.dto.GoogleUserInfo;
import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.service.GoogleAuthService;
import com.example.climbingnav.auth.service.RefreshTokenService;
import com.example.climbingnav.global.base.ApiResponse;
import com.example.climbingnav.global.base.types.ResponseCode;
import com.example.climbingnav.global.exception.CustomException;
import com.example.climbingnav.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/google")
public class GoogleOAuthController {
    private final GoogleAuthService googleAuthService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.jwt.access-seconds}")
    private String accessSeconds;

    @Value("${app.jwt.refresh-seconds}")
    private long refreshSeconds;

    @PostMapping("/exchange")
    public ResponseEntity<ApiResponse<Map<String, Object>>> exchange(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (!StringUtils.hasText(code)) {
            throw new CustomException(ResponseCode.BAD_REQUEST, "code 값이 없습니다.");
        }

        GoogleTokenResponse googleToken = googleAuthService.exchangeCodeForToken(code);

        GoogleUserInfo userInfo = googleAuthService.fetchUserInfo(googleToken.accessToken());

        User user = googleAuthService.upsertFromGoogle(userInfo, googleToken);

        String refreshToken = jwtUtil.createRefresh(user.getId().toString());
        String accessToken  = jwtUtil.createAccess(user.getId().toString(), Map.of(
                "email", user.getEmail(),
                "nickname", user.getNickname()
        ));

        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofSeconds(refreshSeconds))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        headers.add("X-Access-Seconds", accessSeconds);
        headers.add("X-Refresh-Seconds", String.valueOf(refreshSeconds));

        Map<String, Object> responseBody = Map.of(
                "nickname", user.getNickname(),
                "email", user.getEmail(),
                "avatar", user.getAvatarUrl()
        );

        return ResponseEntity.ok().headers(headers).body(ApiResponse.ok(responseBody));
    }
}
