package com.example.climbingnav.community.service;

import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.repository.UserRepository;
import com.example.climbingnav.community.Repository.PostRepository;
import com.example.climbingnav.community.dto.PostSaveRequest;
import com.example.climbingnav.community.entity.Post;
import com.example.climbingnav.community.entity.constants.StatusType;
import com.example.climbingnav.global.base.UserVo;
import com.example.climbingnav.global.base.types.ResponseCode;
import com.example.climbingnav.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createPost(UserVo userVo, PostSaveRequest postSaveRequest, List<MultipartFile> files) {
        User user = userRepository.findByEmail(userVo.email())
                .orElseThrow(() -> new CustomException(ResponseCode.UNAUTHORIZED, "해당 유저 계정은 존재하지 않는 계정입니다."));

        Post post = Post.builder()
                .title(postSaveRequest.title())
                .content(postSaveRequest.content())
                .user(user)
                .status(StatusType.ACTIVE)
                .build();

        if (files != null) {
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;


            }
        }

        Post savedPost = postRepository.save(post);
    }
}
