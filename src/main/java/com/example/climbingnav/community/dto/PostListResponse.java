package com.example.climbingnav.community.dto;

public record PostListResponse(
        Long id,
       String title,
       String author,
       String avatarUrl,
       String content,
       int likeCount,
       int commentsCount,
       String createdAt
) {}
