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

    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, String>> save(@AuthenticationPrincipal UserVo userVo,
                                      @RequestPart("post") @Valid PostSaveRequest postSaveRequest,
                                      @RequestPart(value = "files", required = false)List<MultipartFile> files) {
        Long postId = postService.createPost(userVo, postSaveRequest, files);

        return ApiResponse.ok(Map.of("postId", postId.toString()));
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostDetailResponse> getOnePost(@PathVariable Long postId,
                                                      @AuthenticationPrincipal UserVo userVo) {
        return ApiResponse.ok(postService.getPostDetail(postId, userVo));
    }

    @GetMapping
    public ApiResponse<PostSliceResponse<PostListResponse>> getAllPosts(@RequestParam(required = false) Long cursorId,
                                                      @RequestParam(required = false) String boardCode,
                                                      @AuthenticationPrincipal UserVo userVo) {
        return ApiResponse.ok(postService.getAllPosts(boardCode, cursorId, userVo));
    }

    @GetMapping("/my-posts")
    public ApiResponse<PostSliceResponse<MyPostListResponse>> viewMyPosts(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) String boardCode,
            @AuthenticationPrincipal UserVo userVo) {
        return ApiResponse.ok(postService.getMyPostsList(boardCode, cursorId, userVo));
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
