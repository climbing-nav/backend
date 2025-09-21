package com.example.climbingnav.auth.controller;

import com.example.climbingnav.auth.config.KakaoProperties;
import com.example.climbingnav.auth.dto.KakaoTokenResponse;
import com.example.climbingnav.auth.dto.KakaoUserInfo;
import com.example.climbingnav.auth.service.KakaoAuthService;
import com.example.climbingnav.global.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class AuthController {
    private final KakaoAuthService kakaoAuthService;
    private final KakaoProperties kakaoProperties;
    private final JwtUtil jwtUtil;

    public AuthController(KakaoAuthService kakaoAuthService, KakaoProperties props) {
        this.kakaoAuthService = kakaoAuthService;
        this.kakaoProperties = props;
        this.jwtUtil = new JwtUtil(props);
    }

    @GetMapping("/auth/kakao/login")
    public ResponseEntity<Void> kakaoLogin() {
        String state = UUID.randomUUID().toString();
        String url = kakaoAuthService.buildAuthorizeUrl(state);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", url)
                .build();
    }

    @GetMapping("/auth/kakao/callback")
    public ResponseEntity<Void> callback(@RequestParam("code") String code,
                                         @RequestParam(value = "state", required = false) String state,
                                         HttpServletResponse resp) {
        KakaoTokenResponse token = kakaoAuthService.exchangeCodeForToken(code);
        KakaoUserInfo user = kakaoAuthService.fetchUserInfo(token.getAccessToken());


        Map<String, Object> claims = new HashMap<>();
        claims.put("kid", user.getId());
        if (user.getKakaoAccount() != null) {
            claims.put("email", user.getKakaoAccount().getEmail());
            if (user.getKakaoAccount().getProfile() != null) {
                claims.put("nickname", user.getKakaoAccount().getProfile().getNickname());
                claims.put("avatar", user.getKakaoAccount().getProfile().getProfileImageUrl());
            }
        }
        String jwtToken = jwtUtil.createToken("kakao:" + user.getId(), claims);

        Cookie cookie = new Cookie("APP_SESSION", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        resp.addHeader("Set-Cookie", "APP_SESSION=" + jwtToken + "; HttpOnly; Secure; Path=/; SameSite=Lax");


        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", kakaoProperties.getFrontendRedirect())
                .build();
    }
}
