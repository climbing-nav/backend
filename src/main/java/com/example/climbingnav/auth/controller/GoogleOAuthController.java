package com.example.climbingnav.auth.controller;

import com.example.climbingnav.auth.client.GoogleOAuthClient;
import com.example.climbingnav.auth.client.OAuthResponseBuilder;
import com.example.climbingnav.auth.dto.GoogleTokenResponse;
import com.example.climbingnav.auth.dto.GoogleUserInfo;
import com.example.climbingnav.auth.dto.OAuthTokenResponse;
import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.service.GoogleAccountService;
import com.example.climbingnav.global.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/google")
public class GoogleOAuthController {
    private final GoogleOAuthClient googleOAuthClient;
    private final GoogleAccountService googleAccountService;
    private final JwtUtil jwtUtil;
    private final OAuthResponseBuilder oAuthResponseBuilder;

    @Value("${app.frontend.success-redirect}")
    private String successRedirect;

    @Value("${app.frontend.failure-redirect}")
    private String failureRedirect;

    @Value("${app.jwt.access-seconds}")
    private String accessSeconds;

    @Value("${app.jwt.refresh-seconds}")
    private String refreshSeconds;

    private final Map<String, Boolean> stateStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    @GetMapping("/login")
    public ResponseEntity<Void> login(HttpServletResponse response) {
        String state = generateState();
        stateStore.put(state, Boolean.TRUE);
        String redirect = googleOAuthClient.buildAuthRedirectUrl(state);
        response.setHeader("Location", redirect);
        return ResponseEntity.status(302).build();
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpServletResponse response
    ) {
        try {
            if (StringUtils.hasText(error)) {
                response.setHeader("Location", failureRedirect + "?error=" + error);
                return ResponseEntity.status(302).build();
            }
            if (!StringUtils.hasText(code) || !StringUtils.hasText(state) || !stateStore.containsKey(state)) {
                response.setHeader("Location", failureRedirect + "?error=invalid_state");
                return ResponseEntity.status(302).build();
            }
            stateStore.remove(state);

            GoogleTokenResponse googleToken = googleOAuthClient.exchangeCodeForToken(code);
            GoogleUserInfo userInfo = googleOAuthClient.fetchUserInfo(googleToken.accessToken());

            User user = googleAccountService.upsertFromGoogle(userInfo, googleToken);

            String subject = String.valueOf(user.getId());

            String refreshToken = jwtUtil.createRefresh(user.getId().toString());
            String accessToken = jwtUtil.createAccess(subject, Map.of(
                    "email", user.getEmail(),
                    "nickname", user.getNickname()
            ));

            OAuthTokenResponse tokens = new OAuthTokenResponse(accessToken, refreshToken, accessSeconds, refreshSeconds);
            return oAuthResponseBuilder.successRedirect(successRedirect, tokens);
        } catch (Exception e) {
            response.setHeader("Location", failureRedirect + "?error=" + e.getMessage());
            return ResponseEntity.status(302).build();
        }

    }

    private String generateState() {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
