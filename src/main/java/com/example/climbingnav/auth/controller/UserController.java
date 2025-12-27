package com.example.climbingnav.auth.controller;

import com.example.climbingnav.auth.dto.UserProfileResponse;
import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.service.UserService;
import com.example.climbingnav.global.base.ApiResponse;
import com.example.climbingnav.global.base.UserVo;
import com.example.climbingnav.global.base.types.ResponseCode;
import com.example.climbingnav.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/user")
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> myProfile(@AuthenticationPrincipal(expression = "principal") String userId) {
        if (userId == null || userId.isBlank()) {
            throw new CustomException(ResponseCode.BAD_REQUEST, "userId 값이 비어있습니다.");
        }

        User user = userService.getUserProfile(userId);

        return ResponseEntity.ok(
                ApiResponse.ok(
                        new UserProfileResponse(
                                user.getId(),
                                user.getEmail(),
                                user.getNickname(),
                                user.getAvatarUrl()
                        )
                )
        );
    }

    @GetMapping("/mypage/count")
    public ApiResponse<Long> myPostsCount(@AuthenticationPrincipal UserVo userVo) {
        return ApiResponse.ok(userService.getMyPostsCount(userVo));
    }
}
