package com.example.climbingnav.auth.dto;

public record UserProfileResponse(
        Long id,
        String email,
        String nickname,
        String avatarUrl
) {}
