package com.example.climbingnav.auth.controller;

import com.example.climbingnav.auth.dto.KakaoTokenResponse;
import com.example.climbingnav.auth.dto.KakaoUserInfo;
import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.service.KakaoAuthService;
import com.example.climbingnav.auth.service.UserService;
import com.example.climbingnav.global.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequestMapping("/auth/kakao")
@RequiredArgsConstructor
@RestController
public class KakaoAuthController {
    private final KakaoAuthService kakaoAuthService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Value("${kakao.frontend-redirect}")
    private String frontendRedirect;

    @GetMapping("/login")
    public ResponseEntity<Void> kakaoLogin() {
        String state = UUID.randomUUID().toString();
        String url = kakaoAuthService.buildAuthorizeUrl(state);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", url)
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam("code") String code,
                                         @RequestParam(value = "state", required = false) String state,
                                         HttpServletResponse resp) {
        KakaoTokenResponse token = kakaoAuthService.exchangeCodeForToken(code);
        KakaoUserInfo kakaoUser = kakaoAuthService.fetchUserInfo(token.getAccessToken());

        User user = userService.upsertFromKakao(kakaoUser);

        String refresh = jwtUtil.createRefresh(user.getId().toString());

        String cookie = "REFRESH=" + refresh
                + "; HttpOnly; Path=/; Max-Age=" + (30L * 24 * 3600)
                + "; SameSite=None; Secure";
        resp.addHeader("Set-Cookie", cookie);


        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", frontendRedirect)
                .build();
    }

    private static Map<String, Object> getKakaoUserObjectMap(KakaoUserInfo kakaoUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("kid", kakaoUser.getId());
        claims.put("kp", "kakao");
        if (kakaoUser.getKakaoAccount() != null) {
            claims.put("email", kakaoUser.getKakaoAccount().getEmail());
            if (kakaoUser.getKakaoAccount().getProfile() != null) {
                claims.put("nickname", kakaoUser.getKakaoAccount().getProfile().getNickname());
                claims.put("avatar", kakaoUser.getKakaoAccount().getProfile().getProfileImageUrl());
            }
        }
        return claims;
    }
}
