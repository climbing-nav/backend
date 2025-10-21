package com.example.climbingnav.auth.dto;

public record OAuthTokenResponse(
        String access,
        String refresh,
        String accessSeconds,
        String refreshSeconds
) {}
