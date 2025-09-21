package com.example.climbingnav.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_provider_uid",
        columnNames = {"provider", "provider_user_id"}))
public class UserSocialAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 20, nullable = false)
    private String provider; // "kakao"

    @Column(length = 64, nullable = false)
    private String providerUserId;

    @Column(length = 512)
    private String scope;

    private LocalDateTime connectedAt;

    private LocalDateTime unlinkAt;

    private String refreshTokenCipher;

    private LocalDateTime refreshTokenExpiresAt;
}
