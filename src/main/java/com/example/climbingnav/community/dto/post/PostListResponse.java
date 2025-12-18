package com.example.climbingnav.community.dto.post;

import com.example.climbingnav.community.entity.Comment;

import java.util.List;

public record PostListResponse(
        Long id,
       String title,
       String author,
       String avatarUrl,
       String content,
       Long likeCount,
       List<Long> commentIds,
       int commentsCount,
       String categoryName,
       boolean isLiked,
       String createdAt
) {}
