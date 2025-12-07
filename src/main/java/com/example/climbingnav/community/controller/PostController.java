package com.example.climbingnav.community.controller;

import com.example.climbingnav.community.dto.PostDetailResponse;
import com.example.climbingnav.community.dto.PostSaveRequest;
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
    public ApiResponse<Map<String, String>> savePost(@AuthenticationPrincipal UserVo userVo,
                                      @RequestBody @Valid PostSaveRequest postSaveRequest,
                                      @RequestPart(value = "files", required = false)List<MultipartFile> files) {
        log.info("게시글 작성 api 호출 성공! usernickname:{}", userVo.nickname());
        Long postId = postService.createPost(userVo, postSaveRequest, null);

        return ApiResponse.ok(Map.of("postId", postId.toString()));
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostDetailResponse> getOnePost(@AuthenticationPrincipal UserVo userVo,
                                                    @PathVariable Long postId) {
        return ApiResponse.ok(postService.getPostDetail(userVo, postId));
    }
}
