package com.example.climbingnav.community.dto.post;

public record MyPostListResponse(
        Long id,
        String title,
        String author,
        String content,
        Long likeCount,
        long commentsCount,
        String categoryName,
        boolean isLiked,
        String createdAt
) {}
