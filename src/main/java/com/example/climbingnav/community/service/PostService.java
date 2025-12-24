package com.example.climbingnav.community.service;

import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.repository.UserRepository;
import com.example.climbingnav.community.Repository.*;
import com.example.climbingnav.community.dto.file.FileResponse;
import com.example.climbingnav.community.dto.file.UploadResult;
import com.example.climbingnav.community.dto.post.*;
import com.example.climbingnav.community.entity.Category;
import com.example.climbingnav.community.entity.Post;
import com.example.climbingnav.community.entity.PostLike;
import com.example.climbingnav.community.entity.UploadFile;
import com.example.climbingnav.community.entity.constants.StatusType;
import com.example.climbingnav.global.base.UserVo;
import com.example.climbingnav.global.base.types.ResponseCode;
import com.example.climbingnav.global.client.S3Uploader;
import com.example.climbingnav.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostLikeRepository postLikeRepository;
    private final UploadFileRepository uploadFileRepository;
    private final CommentRepository commentRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public Long createPost(UserVo userVo, PostSaveRequest postSaveRequest, List<MultipartFile> files) {
        Category category = categoryRepository.findByCode(postSaveRequest.boardCode())
                .orElseThrow(() -> new CustomException(ResponseCode.BAD_REQUEST, "존재하지 않는 게시판입니다."));

        User user = userRepository.findByEmail(userVo.email())
                .orElseThrow(() -> new CustomException(ResponseCode.UNAUTHORIZED, "해당 계정은 존재하지 않는 계정입니다."));

        Post savedPost = postRepository.save(Post.builder()
                .title(postSaveRequest.title())
                .content(postSaveRequest.content())
                .category(category)
                .likeCount(0L)
                .user(user)
                .status(StatusType.ACTIVE)
                .build());

        saveFiles(savedPost, userVo.userId(), files);

        return savedPost.getId();
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(Long postId, UserVo userVo) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND, "조회하신 게시글을 찾을 수 없습니다."));

        boolean isLiked = postLikeRepository.existsByUser_IdAndPost_Id(userVo.userId(), postId);

        if (post.getStatus() != StatusType.ACTIVE) {
            throw new CustomException(ResponseCode.BAD_REQUEST, "이미 삭제되었거나 비활성화된 게시글입니다.");
        }

        List<FileResponse> files = uploadFileRepository.findAllByPost_Id(postId).stream()
                .map(f -> new FileResponse(
                        f.getId(),
                        s3Uploader.generatePresignedGetUrl(f.getS3Key())
                ))
                .toList();

        return PostDetailResponse.from(post, isLiked, files);
    }

    @Transactional(readOnly = true)
    public PostSliceResponse getPostsList(String boardCode, Long cursorId, UserVo userVo) {
        List<Post> posts = postRepository.findActivePostsByCategory(
                boardCode, cursorId, PageRequest.of(0, 21)
        );

        boolean hasNext = posts.size() == 21;

        List<Post> displayPosts = hasNext ? posts.subList(0, 20) : posts;

        Long nextCursorId = hasNext ? posts.get(19).getId() : null;

        List<Long> postIds = displayPosts.stream()
                .map(Post::getId)
                .toList();

        Set<Long> likedPostIds = userVo.userId() == null
                ? Set.of()
                : new HashSet<>(postLikeRepository.findLikedPostIds(userVo.userId(), postIds));

        List<UploadFile> files = uploadFileRepository.findFilesByPostIds(postIds);

        Map<Long, List<FileResponse>> fileMap = files.stream()
                .collect(Collectors.groupingBy(
                        f -> f.getPost().getId(),
                        Collectors.mapping(
                                f -> new FileResponse(f.getId(), s3Uploader.
                                        generatePresignedGetUrl(f.getS3Key())),
                                Collectors.toList()
                        )
                ));

        List<PostListResponse> postList = displayPosts.stream()
                .map(p -> new PostListResponse(
                        p.getId(),
                        p.getTitle(),
                        p.getUser().getNickname(),
                        p.getUser().getAvatarUrl(),
                        p.getContent(),
                        p.getLikeCount(),
                        p.getComments().stream()
                                .filter(c -> c.getStatus().equals(StatusType.ACTIVE))
                                .count(),
                        p.getCategory().getName(),
                        fileMap.getOrDefault(p.getId(), List.of()),
                        likedPostIds.contains(p.getId()),
                        p.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                ))
                .toList();

        return new PostSliceResponse(postList, hasNext, nextCursorId);
    }

    @Transactional(readOnly = true)
    public String getMyPostsList(UserVo userVo) {
        List<Post> posts = postRepository.
                findByUser_IdAndStatusOrderByIdDesc(userVo.userId(), StatusType.ACTIVE);

        return "Success";
    }

    @Transactional
    public String updatePostStatusToDelete(Long postId, UserVo userVo) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND, "존재하지 않는 게시글입니다."));

        if(!post.getUser().getEmail().equals(userVo.email())) {
            throw new CustomException(ResponseCode.FORBIDDEN, "해당 게시글 작성자만 삭제가 가능합니다.");
        }

        if(post.getStatus() == StatusType.DELETED) {
            throw new CustomException(ResponseCode.NOT_FOUND, "조회하신 게시글은 이미 삭제되었습니다.");
        }

        post.changeStatus(StatusType.DELETED);

        commentRepository.updateStatusByPostId(post.getId(), StatusType.DELETED);

        return "success";
    }

    @Transactional
    public void updatePost(Long postId, UserVo userVo, PostUpdateRequest postUpdateRequest) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND, "존재하지 않는 게시글입니다."));

        if(!post.getUser().getEmail().equals(userVo.email())) {
            throw new CustomException(ResponseCode.FORBIDDEN, "해당 게시글의 작성자만 수정이 가능합니다.");
        }

        post.update(postUpdateRequest.title(), postUpdateRequest.content(), postUpdateRequest.boardCode());
    }

    @Transactional
    public LikeToggleResponse toggleLike(UserVo userVo, Long postId) {
        Post post = postRepository.findByIdAndStatus(postId, StatusType.ACTIVE)
                .orElseThrow(() -> new CustomException(ResponseCode.BAD_REQUEST, "존재하지 않는 게시글입니다."));

        Optional<PostLike> existingLike = postLikeRepository.findByUser_IdAndPost_Id(userVo.userId(), post.getId());
        if(existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            post.decreaseLikeCount();
            return new LikeToggleResponse(false, post.getLikeCount());
        }

        User user = userRepository.findById(userVo.userId())
                .orElseThrow(() -> new CustomException(ResponseCode.UNAUTHORIZED, "존재하지 않는 사용자입니다."));

        post.increaseLikeCount();
        postLikeRepository.save(PostLike.of(user, post));
        return new LikeToggleResponse(true, post.getLikeCount());
    }

    private void saveFiles(Post post, Long userId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            UploadResult result = s3Uploader.upload(file, userId);

            uploadFileRepository.save(UploadFile.builder()
                    .post(post)
                    .url(result.url())
                    .s3Key(result.key())
                    .originalName(result.originalName())
                    .size(result.size())
                    .build());
        }
    }
}
