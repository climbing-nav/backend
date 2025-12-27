package com.example.climbingnav.community.dto.post;

import java.util.List;

public record PostSliceResponse<T> (
        List<T> posts,
        boolean hasNext,
        Long nextCursorId
) {}
