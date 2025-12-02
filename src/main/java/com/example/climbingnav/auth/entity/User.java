package com.example.climbingnav.auth.entity;

import com.example.climbingnav.auth.dto.KakaoUserInfo;
import com.example.climbingnav.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditOverride;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@AuditOverride(forClass = BaseEntity.class)
@Table(name = "users")
@Entity
public class User extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private Boolean emailVerified = false;

    @Column(length = 100)
    private String nickname;

    @Column(columnDefinition = "text")
    private String avatarUrl;

    private void changeEmail(String email) {
        if (email == null || email.isBlank()) return;
        this.email = email.trim().toLowerCase();
    }

    private void renameTo(String nickname) {
        if (nickname == null || nickname.isBlank()) return;
        this.nickname = nickname.trim();
    }

    private void changeAvatarTo(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) return;
        this.avatarUrl = avatarUrl.trim();
    }

    public void updateKakaoAccount(KakaoUserInfo.KakaoAccount account) {
        if (account == null) return;
        changeEmail(account.getEmail());

        var profile = account.getProfile();
        if (profile != null) {
            renameTo(profile.getNickname());
            changeAvatarTo(profile.getProfileImageUrl());
        }
    }
}
