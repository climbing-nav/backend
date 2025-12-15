package com.example.climbingnav.community.controller;

import com.example.climbingnav.community.dto.post.*;
import com.example.climbingnav.community.service.PostService;
import com.example.climbingnav.global.base.ApiResponse;
import com.example.climbingnav.global.base.UserVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@RestController
public class PostController {
    private final PostService postService;

    @PostMapping(value = "/save", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ApiResponse<Map<String, String>> save(@AuthenticationPrincipal UserVo userVo,
                                      @RequestBody @Valid PostSaveRequest postSaveRequest,
                                      @RequestPart(value = "files", required = false)List<MultipartFile> files) {
        Long postId = postService.createPost(userVo, postSaveRequest, null);

        return ApiResponse.ok(Map.of("postId", postId.toString()));
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostDetailResponse> getOnePost(@PathVariable Long postId,
                                                      @AuthenticationPrincipal UserVo userVo) {
        return ApiResponse.ok(postService.getPostDetail(postId, userVo));
    }

    @GetMapping
    public ApiResponse<PostSliceResponse> getAllPosts(@RequestParam(required = false) Long cursorId,
                                                      @RequestParam(required = false) String boardCode,
                                                      @AuthenticationPrincipal UserVo userVo) {
        return ApiResponse.ok(postService.getPostsList(boardCode, cursorId, userVo));
    }

    @GetMapping("/my-posts")
    public ApiResponse<?> viewMyWriting(@AuthenticationPrincipal UserVo userVo) {
        // 내가 쓴 게시글 목록 조회
        String content = postService.getMyPostsList(userVo);

        return null;
    }

    @PatchMapping("/{postId}")
    public ApiResponse<?> update(@PathVariable Long postId,
                                 @AuthenticationPrincipal UserVo userVo,
                                 @RequestBody PostUpdateRequest postUpdateRequest) {
        postService.updatePost(postId, userVo, postUpdateRequest);

        return null;
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<String> delete(@PathVariable Long postId,
                                      @AuthenticationPrincipal UserVo userVo) {
        return ApiResponse.ok(postService.updatePostStatusToDelete(postId, userVo));

    }

    @PostMapping("{postId}/like")
    public ApiResponse<LikeToggleResponse> likeStatus(@AuthenticationPrincipal UserVo userVo,
                                     @PathVariable Long postId) {
        return ApiResponse.ok(postService.toggleLike(userVo, postId));
    }
}
