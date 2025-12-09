package com.example.climbingnav.community.dto;

public record PostUpdateRequest(
        Long id,
        String title,
        String content
) {}
