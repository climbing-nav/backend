package com.example.climbingnav.community.dto.comment;

import com.example.climbingnav.community.entity.Comment;

import java.time.format.DateTimeFormatter;

public record CommentsResponse(
        String author,
        String avatarUrl,
        String content,
        String createdAt
) {
    public static CommentsResponse from(Comment comment) {
        return new CommentsResponse(
                comment.getUser().getNickname(),
                comment.getUser().getAvatarUrl(),
                comment.getContent(),
                comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
    }
}
