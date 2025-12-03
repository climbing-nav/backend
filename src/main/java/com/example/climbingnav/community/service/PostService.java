package com.example.climbingnav.community.service;

import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.repository.UserRepository;
import com.example.climbingnav.community.Repository.CategoryRepository;
import com.example.climbingnav.community.Repository.PostRepository;
import com.example.climbingnav.community.dto.PostDetailResponse;
import com.example.climbingnav.community.dto.PostSaveRequest;
import com.example.climbingnav.community.entity.Category;
import com.example.climbingnav.community.entity.Post;
import com.example.climbingnav.community.entity.constants.StatusType;
import com.example.climbingnav.global.base.UserVo;
import com.example.climbingnav.global.base.types.ResponseCode;
import com.example.climbingnav.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Long createPost(UserVo userVo, PostSaveRequest postSaveRequest, List<MultipartFile> files) {
        Category category = categoryRepository.findByCode(postSaveRequest.boardCode())
                .orElseThrow(() -> new CustomException(ResponseCode.BAD_REQUEST, "존재하지 않는 게시판입니다."));

        User user = userRepository.findByEmail(userVo.email())
                .orElseThrow(() -> new CustomException(ResponseCode.UNAUTHORIZED, "해당 유저 계정은 존재하지 않는 계정입니다."));

        Post post = Post.builder()
                .title(postSaveRequest.title())
                .content(postSaveRequest.content())
                .category(category)
                .user(user)
                .status(StatusType.ACTIVE)
                .build();

        if (files != null) {
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;


            }
        }

        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(UserVo userVo, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND, "조회하신 게시글을 찾을 수 없습니다."));

        if (post.getStatus() != StatusType.ACTIVE) {
            throw new CustomException(ResponseCode.BAD_REQUEST, "이미 삭제되었거나 비활성화된 게시글입니다.");
        }

        return PostDetailResponse.from(post);
    }

    @Transactional(readOnly = true)
    public void getPostsList() {

    }
}
