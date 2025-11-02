package com.example.climbingnav.auth.service;

import com.example.climbingnav.auth.dto.GoogleTokenResponse;
import com.example.climbingnav.auth.dto.GoogleUserInfo;
import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.entity.UserSocialAccount;
import com.example.climbingnav.auth.repository.UserRepository;
import com.example.climbingnav.auth.repository.UserSocialAccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
@Service
public class GoogleAuthService {
    @Value("${google.client-id}")
    private  String clientId;

    @Value("${google.client-secret}")
    private  String clientSecret;

    @Value("${google.redirect-uri}")
    private  String redirectUri;

    @Value("${google.token-uri}")
    private  String tokenUri;

    @Value("${google.userinfo-uri}")
    private  String userInfoUrl;

    private final UserRepository userRepository;
    private final UserSocialAccountRepository socialAccountRepository;
    private final RestClient restClient;

    public GoogleAuthService(
            RestClient restClient,
            UserRepository userRepository,
            UserSocialAccountRepository socialAccountRepository
    ) {
        this.restClient = restClient;
        this.userRepository = userRepository;
        this.socialAccountRepository = socialAccountRepository;
    }


    @Transactional
    public User upsertFromGoogle(GoogleUserInfo googleUserInfo, GoogleTokenResponse googleTokenResponse) {
        UserSocialAccount existingUser = socialAccountRepository.findByProviderAndProviderUserId(
                "google", googleUserInfo.sub()).orElse(null);

        User user;
        if (existingUser != null) {
            user = existingUser.getUser();
        } else {
            user = (googleUserInfo.email() != null)
                    ? userRepository.findByEmail(googleUserInfo.email().toLowerCase()).orElseGet(User::new)
                    : new User();
        }

        if (googleUserInfo.email() != null && !googleUserInfo.email().isBlank()) {
            user.setEmail(googleUserInfo.email().trim().toLowerCase());
        }

        if (googleUserInfo.emailVerified()) {
            user.setEmailVerified(true);
        }

        if (googleUserInfo.name() != null && !googleUserInfo.name().isBlank()) {
            user.setNickname(googleUserInfo.name().trim());
        }
        if (googleUserInfo.pictureUrl() != null && !googleUserInfo.pictureUrl().isBlank()) {
            user.setAvatarUrl(googleUserInfo.pictureUrl().trim());
        }

        user = userRepository.save(user);

        if (existingUser == null) {
            UserSocialAccount socialAccount = UserSocialAccount.builder()
                    .user(user)
                    .provider("google")
                    .providerUserId(googleUserInfo.sub())
                    .scope(googleTokenResponse.scope())
                    .connectedAt(LocalDateTime.now())
                    .build();

            socialAccountRepository.save(socialAccount);
        } else {
            if (googleTokenResponse.scope() != null
                    && (existingUser.getScope() == null || !googleTokenResponse.scope().equals(existingUser.getScope()))) {
                existingUser.updateScope(googleTokenResponse.scope());
                socialAccountRepository.save(existingUser);
            }
        }

        return user;
    }

    public GoogleTokenResponse exchangeCodeForToken(String code) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("grant_type", "authorization_code");

        return restClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(GoogleTokenResponse.class);
    }

    public GoogleUserInfo fetchUserInfo(String accessToken) {
        return restClient.get()
                .uri(userInfoUrl)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(GoogleUserInfo.class);
    }
}
