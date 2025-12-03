package com.example.climbingnav.community.dto;

import jakarta.validation.constraints.NotBlank;

public record PostSaveRequest(
        @NotBlank
        String title,

        @NotBlank
        String content,

        @NotBlank
        String boardCode
) {}
