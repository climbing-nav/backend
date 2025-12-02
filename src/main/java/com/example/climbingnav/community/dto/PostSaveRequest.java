package com.example.climbingnav.community.dto;

import com.example.climbingnav.community.entity.Post;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record PostSaveRequest(
        @NotBlank
        String title,

        @NotBlank
        String content
) {}
