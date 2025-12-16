package com.example.climbingnav.community.dto.file;

public record UploadResult(
        String key,
        String url,
        String originalName,
        String contentType,
        long size
) {}
