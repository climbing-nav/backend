package com.example.climbingnav.auth.service;

import com.example.climbingnav.auth.dto.KakaoTokenResponse;
import com.example.climbingnav.auth.dto.KakaoUserInfo;
import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.entity.UserSocialAccount;
import com.example.climbingnav.auth.repository.UserRepository;
import com.example.climbingnav.auth.repository.UserSocialAccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserSocialAccountRepository socialAccountRepository;

    @Transactional
    public User upsertFromKakao(KakaoUserInfo kakaoUserInfo) {
        String provider = "kakao";
        String providerUserId = String.valueOf(kakaoUserInfo.getId());

        Optional<UserSocialAccount> existUser = socialAccountRepository.
                findByProviderAndProviderUserId(provider, providerUserId);

        var account = kakaoUserInfo.getKakaoAccount();
        var profile = (account != null) ? account.getProfile() : null;

        String email     = (account != null) ? account.getEmail() : null;
        String nickname  = (profile != null) ? profile.getNickname() : null;
        String avatarUrl = (profile != null) ? profile.getProfileImageUrl() : null;

        User.UserBuilder userBuilderBuilder = existUser
                .map(link -> link.getUser().toBuilder())
                .orElse(User.builder());

        if (email != null)     userBuilderBuilder.email(email);
        if (nickname != null)  userBuilderBuilder.nickname(nickname);
        if (avatarUrl != null) userBuilderBuilder.avatarUrl(avatarUrl);

        User user = userBuilderBuilder.build();

        user = userRepository.save(user);

        if (existUser.isEmpty()) {
            socialAccountRepository.save(UserSocialAccount.builder()
                    .user(user)
                    .provider(provider)
                    .providerUserId(providerUserId)
                    .connectedAt(LocalDateTime.now())
                    .build());
        }

        return user;
    }
}
