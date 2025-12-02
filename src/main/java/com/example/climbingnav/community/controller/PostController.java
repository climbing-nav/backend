package com.example.climbingnav.community.controller;

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

@Slf4j
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@RestController
public class PostController {
    private final PostService postService;

    @PostMapping(value = "/save", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ApiResponse<Void> savePost(@AuthenticationPrincipal UserVo userVo,
                                      @RequestBody @Valid PostSaveRequest postSaveRequest,
                                      @RequestPart(value = "files", required = false)List<MultipartFile> files) {

        postService.createPost(userVo, postSaveRequest, null);

        return ApiResponse.ok(null);
    }
}
