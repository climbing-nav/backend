package com.example.climbingnav.community.dto.post;

public record PostListResponse(
        Long id,
       String title,
       String author,
       String avatarUrl,
       String content,
       Long likeCount,
       int commentsCount,
       String categoryName,
       String createdAt
) {}
