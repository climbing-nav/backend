package com.example.climbingnav.community.dto.post;

import com.example.climbingnav.community.dto.file.FileResponse;
import com.example.climbingnav.community.entity.Comment;

import java.util.List;

public record PostListResponse(
        Long id,
       String title,
       String author,
       String avatarUrl,
       String content,
       Long likeCount,
       long commentsCount,
       String categoryName,
       List<FileResponse> files,
       boolean isLiked,
       String createdAt
) {}
