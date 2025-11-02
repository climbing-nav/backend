package com.example.climbingnav.auth.controller;

import com.example.climbingnav.auth.dto.KakaoTokenResponse;
import com.example.climbingnav.auth.dto.KakaoUserInfo;
import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.service.KakaoAuthService;
import com.example.climbingnav.auth.service.UserService;
import com.example.climbingnav.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/api/auth/kakao")
@RequiredArgsConstructor
@RestController
public class KakaoAuthController {
    private final KakaoAuthService kakaoAuthService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.refresh-seconds}")
    private String refreshSeconds;

    @Value("${app.jwt.access-seconds}")
    private String accessSeconds;

    @PostMapping("/exchange")
    public ResponseEntity<Map<String, Object>> exchangeToken(@RequestBody Map<String, String> kakaoCodeInfo) {
        String code = kakaoCodeInfo.get("code");

        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "empty_code"));
        }

        KakaoTokenResponse kakaoToken = kakaoAuthService.exchangeCodeForToken(code);
        KakaoUserInfo kakaoUser = kakaoAuthService.fetchUserInfo(kakaoToken.getAccessToken());

        User user = kakaoAuthService.upsertFromKakao(kakaoUser);

        // refresh token 저장 방식 결정 후 로직 구현 예정
//        String refresh = jwtUtil.createRefresh(user.getId().toString());
        String access = jwtUtil.createAccess(user.getId().toString(), Map.of(
                "email", user.getEmail(),
                "nickname", user.getNickname()
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + access);

        Map<String, Object> responseBody = Map.of(
                "email", user.getEmail(),
                "nickname", user.getNickname()
        );

        headers.add("X-Access-Seconds", accessSeconds);
        headers.add("X-Refresh-Seconds", refreshSeconds);

        return ResponseEntity.ok().headers(headers).body(responseBody);
    }

}
