package com.example.climbingnav.community.dto;

import com.example.climbingnav.community.entity.Post;
import com.example.climbingnav.community.entity.UploadFile;

import java.time.format.DateTimeFormatter;
import java.util.List;

public record PostDetailResponse(
        Long id,
        String category,
        String title,
        String content,
        String author,
        String avatarUrl,

        List<CommentsResponse> comments,
        int likeCount,
        List<String> fileNames,

        String createdAt
) {
    public static PostDetailResponse from(Post post) {
        return new PostDetailResponse(
                post.getId(),
                post.getCategory().getName(),
                post.getTitle(),
                post.getContent(),
                post.getUser().getNickname(),
                post.getUser().getAvatarUrl(),
                post.getComments().stream()
                        .map(CommentsResponse::from)
                        .toList(),
                post.getLikes().size(),
                post.getFiles().stream()
                        .map(UploadFile::getStoredName)
                        .toList(),
                post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
    }
}
