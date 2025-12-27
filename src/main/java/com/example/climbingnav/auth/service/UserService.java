package com.example.climbingnav.auth.service;

import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.repository.UserRepository;
import com.example.climbingnav.community.Repository.PostRepository;
import com.example.climbingnav.global.base.UserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public User getUserProfile(String userId) {
        if (userId == null) return null;

        return userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public Long getMyPostsCount(UserVo userVo) {
        return postRepository.countMyPosts(userVo.userId());
    }

}
