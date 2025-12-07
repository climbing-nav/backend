package com.example.climbingnav.community.dto;

import java.util.List;

public record PostSliceResponse(
        List<PostListResponse> posts,
        boolean hasNext,
        Long nextCursorId
) {
}
