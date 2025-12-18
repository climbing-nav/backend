package com.example.climbingnav.community.dto.comment;

import com.example.climbingnav.community.entity.Comment;

import java.time.format.DateTimeFormatter;

public record CommentsResponse(
        Long id,
        String author,
        String avatarUrl,
        String content,
        String createdAt
) {
    public static CommentsResponse from(Comment comment) {
        return new CommentsResponse(
                comment.getId(),
                comment.getUser().getNickname(),
                comment.getUser().getAvatarUrl(),
                comment.getContent(),
                comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
    }
}
