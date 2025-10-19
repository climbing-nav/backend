package com.example.climbingnav.auth.controller;

import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/api/user")
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> myProfile(@AuthenticationPrincipal(expression = "principal") String userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        User user = userService.getUserProfile(userId);
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "nickname", user.getNickname(),
                "avatar", user.getAvatarUrl()
        ));
    }
}
