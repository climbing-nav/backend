package com.example.climbingnav.community.dto.post;

public record LikeToggleResponse(
        boolean liked,
        long likeCount
) {}
