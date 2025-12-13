package com.example.climbingnav.community.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentSaveRequest(
        @NotNull
        Long postId,

        @NotBlank
        String author,

        @NotBlank
        String content
) {}
