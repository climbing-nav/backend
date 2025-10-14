package com.example.climbingnav.auth.service;

import com.example.climbingnav.auth.dto.KakaoUserInfo;
import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.entity.UserSocialAccount;
import com.example.climbingnav.auth.repository.UserRepository;
import com.example.climbingnav.auth.repository.UserSocialAccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public User getUserProfile(String userId) {
        if (userId == null) return null;

        return userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
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
