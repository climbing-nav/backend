package com.example.climbingnav.community.dto.post;

import com.example.climbingnav.community.dto.comment.CommentsResponse;
import com.example.climbingnav.community.entity.Post;
import com.example.climbingnav.community.entity.UploadFile;
import com.example.climbingnav.community.entity.constants.StatusType;

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
        Long likeCount,

        boolean isLiked,
        List<String> fileNames,

        String createdAt
) {
    public static PostDetailResponse from(Post post, boolean isLiked) {
        return new PostDetailResponse(
                post.getId(),
                post.getCategory().getName(),
                post.getTitle(),
                post.getContent(),
                post.getUser().getNickname(),
                post.getUser().getAvatarUrl(),
                post.getComments().stream()
                        .filter(c -> c.getStatus().equals(StatusType.ACTIVE))
                        .map(CommentsResponse::from)
                        .toList(),
                post.getLikeCount(),
                isLiked,
                post.getFiles().stream()
                        .map(UploadFile::getUrl)
                        .toList(),
                post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
    }
}
