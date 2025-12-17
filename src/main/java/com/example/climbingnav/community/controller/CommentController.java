package com.example.climbingnav.community.controller;

import com.example.climbingnav.community.dto.comment.CommentSaveRequest;
import com.example.climbingnav.community.service.CommentService;
import com.example.climbingnav.global.base.ApiResponse;
import com.example.climbingnav.global.base.UserVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/comments")
@RestController
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/save")
    public ApiResponse<Map<String, String>> save(@AuthenticationPrincipal UserVo userVo,
                                                 @RequestBody @Valid CommentSaveRequest commentSaveRequest) {
        Long commentId = commentService.saveComment(commentSaveRequest, userVo);

        return ApiResponse.ok(Map.of("commentId", commentId.toString()));
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<String> delete(@PathVariable Long commentId,
                                      @AuthenticationPrincipal UserVo userVo) {
        return ApiResponse.ok(commentService.updateCommentStatusToDelete(commentId, userVo));

    }
}
