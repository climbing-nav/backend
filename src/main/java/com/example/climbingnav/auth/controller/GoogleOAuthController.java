package com.example.climbingnav.auth.controller;

import com.example.climbingnav.auth.dto.GoogleTokenResponse;
import com.example.climbingnav.auth.dto.GoogleUserInfo;
import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.service.GoogleAuthService;
import com.example.climbingnav.global.base.ApiResponse;
import com.example.climbingnav.global.base.types.ResponseCode;
import com.example.climbingnav.global.exception.CustomException;
import com.example.climbingnav.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/google")
public class GoogleOAuthController {
    private final GoogleAuthService googleAuthService;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.access-seconds}")
    private String accessSeconds;

    @Value("${app.jwt.refresh-seconds}")
    private String refreshSeconds;

    @PostMapping("/exchange")
    public ResponseEntity<ApiResponse<Map<String, Object>>> exchange(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (!StringUtils.hasText(code)) {
            throw new CustomException(ResponseCode.BAD_REQUEST, "code 값이 없습니다.");
        }

        GoogleTokenResponse googleToken = googleAuthService.exchangeCodeForToken(code);

        GoogleUserInfo userInfo = googleAuthService.fetchUserInfo(googleToken.accessToken());

        User user = googleAuthService.upsertFromGoogle(userInfo, googleToken);

        String subject = String.valueOf(user.getId());
        // refresh token 저장 방식 결정 후 로직 구현 예정
//        String refreshToken = jwtUtil.createRefresh(subject);
        String accessToken  = jwtUtil.createAccess(subject, Map.of(
                "email", user.getEmail(),
                "nickname", user.getNickname()
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
//        headers.add("X-Refresh-Token", refreshToken);

        headers.add("X-Access-Seconds", accessSeconds);
        headers.add("X-Refresh-Seconds", refreshSeconds);

        Map<String, Object> responseBody = Map.of(
                "nickname", user.getNickname(),
                "email", user.getEmail(),
                "avatar", user.getAvatarUrl()
        );

        return ResponseEntity.ok().headers(headers).body(ApiResponse.ok(responseBody));
    }
}
