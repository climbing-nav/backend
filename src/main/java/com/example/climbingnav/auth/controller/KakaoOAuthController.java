package com.example.climbingnav.auth.controller;

import com.example.climbingnav.auth.dto.KakaoTokenResponse;
import com.example.climbingnav.auth.dto.KakaoUserInfo;
import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.service.KakaoAuthService;
import com.example.climbingnav.auth.service.RefreshTokenService;
import com.example.climbingnav.global.base.ApiResponse;
import com.example.climbingnav.global.exception.CustomException;
import com.example.climbingnav.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

import static com.example.climbingnav.global.base.types.ResponseCode.BAD_REQUEST;

@RequestMapping("/api/auth/kakao")
@RequiredArgsConstructor
@RestController
public class KakaoOAuthController {
    private final KakaoAuthService kakaoAuthService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.jwt.refresh-seconds}")
    private long refreshSeconds;

    @Value("${app.jwt.access-seconds}")
    private String accessSeconds;

    @PostMapping("/exchange")
    public ResponseEntity<ApiResponse<Map<String, Object>>> exchangeToken(@RequestBody Map<String, String> kakaoCodeInfo) {
        String code = kakaoCodeInfo.get("code");

        if (code == null || code.isBlank()) {
            throw new CustomException(BAD_REQUEST, "code 값이 없습니다.");
        }

        KakaoTokenResponse kakaoToken = kakaoAuthService.exchangeCodeForToken(code);
        KakaoUserInfo kakaoUser = kakaoAuthService.fetchUserInfo(kakaoToken.getAccessToken());

        User user = kakaoAuthService.upsertFromKakao(kakaoUser);

        String refresh = jwtUtil.createRefresh(user.getId().toString());
        String access = jwtUtil.createAccess(user.getId().toString(), Map.of(
                "email", user.getEmail(),
                "nickname", user.getNickname()
        ));

        refreshTokenService.saveRefreshToken(user.getId(), refresh);

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH", refresh)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofSeconds(refreshSeconds))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, refreshCookie.toString());
        headers.add("Authorization", "Bearer " + access);
        headers.add("X-Access-Seconds", accessSeconds);
        headers.add("X-Refresh-Seconds", String.valueOf(refreshSeconds));

        Map<String, Object> responseBody = Map.of(
                "email", user.getEmail(),
                "nickname", user.getNickname(),
                "avatar", user.getAvatarUrl()
        );

        return ResponseEntity.ok().headers(headers)
                .body(ApiResponse.ok(responseBody));
    }

}
