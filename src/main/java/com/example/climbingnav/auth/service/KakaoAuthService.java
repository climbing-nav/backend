package com.example.climbingnav.auth.service;

import com.example.climbingnav.auth.dto.KakaoTokenResponse;
import com.example.climbingnav.auth.dto.KakaoUserInfo;
import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.entity.UserSocialAccount;
import com.example.climbingnav.auth.repository.UserRepository;
import com.example.climbingnav.auth.repository.UserSocialAccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class KakaoAuthService {

    private final RestClient restClient;
    private final UserSocialAccountRepository socialAccountRepository;
    private final UserRepository userRepository;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    @Value("${kakao.token-uri}")
    private String tokenUri;

    @Value("${kakao.userinfo-uri}")
    private String userinfoUri;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    public KakaoAuthService(RestClient restClient,
                            UserSocialAccountRepository socialAccountRepository,
                            UserRepository userRepository) {
        this.restClient = restClient;
        this.socialAccountRepository = socialAccountRepository;
        this.userRepository = userRepository;
    }

    public KakaoTokenResponse exchangeCodeForToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        form.add("redirect_uri", redirectUri);
        form.add("code", code);

        Optional.ofNullable(clientSecret)
                .filter(s -> !s.isBlank())
                .ifPresent(s -> form.add("client_secret", s));

        return restClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(KakaoTokenResponse.class);
    }

    @Transactional
    public User upsertFromKakao(KakaoUserInfo kakaoUserInfo) {
        String provider = "kakao";
        String providerUserId = String.valueOf(kakaoUserInfo.getId());

        Optional<UserSocialAccount> socialAccount =
                socialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId);

        if (socialAccount.isPresent()) {
            User user = socialAccount.get().getUser();
            user.updateKakaoAccount(kakaoUserInfo.getKakaoAccount());
            return userRepository.save(user);
        }

        String email = Optional.ofNullable(kakaoUserInfo.getKakaoAccount())
                .map(KakaoUserInfo.KakaoAccount::getEmail)
                .orElse(null);

        User user = (email != null)
                ? userRepository.findByEmail(email).orElseGet(User::new)
                : new User();

        user.updateKakaoAccount(kakaoUserInfo.getKakaoAccount());
        User savedUser = userRepository.save(user);

        if(!socialAccountRepository.existsByProviderAndProviderUserId(provider, providerUserId)) {
            saveSocialAccount(savedUser, provider, providerUserId);
        }

        return savedUser;
    }

    /**
     * 카카오에서 받은 access token으로 사용자 정보 조회
     */
    public KakaoUserInfo fetchUserInfo(String accessToken) {
        return restClient.get()
                .uri(userinfoUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(KakaoUserInfo.class);
    }

    private void saveSocialAccount(User user, String provider, String providerUserId) {
        UserSocialAccount socialAccount = UserSocialAccount.builder()
                .user(user)
                .provider(provider)
                .providerUserId(providerUserId)
                .connectedAt(LocalDateTime.now())
                .build();

        socialAccountRepository.save(socialAccount);
    }
}
