package com.example.climbingnav.auth.service;

import com.example.climbingnav.auth.dto.GoogleTokenResponse;
import com.example.climbingnav.auth.dto.GoogleUserInfo;
import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.entity.UserSocialAccount;
import com.example.climbingnav.auth.repository.UserRepository;
import com.example.climbingnav.auth.repository.UserSocialAccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class GoogleAccountService {
    private final UserRepository userRepository;
    private final UserSocialAccountRepository socialAccountRepository;

    @Transactional
    public User upsertFromGoogle(GoogleUserInfo googleUserInfo, GoogleTokenResponse googletokenResponse) {
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
                    .scope(googletokenResponse.scope())
                    .connectedAt(LocalDateTime.now())
                    .build();

            if (googletokenResponse.refreshToken() != null) {

            }
        }

        return user;
    }
}
