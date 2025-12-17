package com.example.climbingnav.community.service;


import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.auth.repository.UserRepository;
import com.example.climbingnav.community.Repository.CommentRepository;
import com.example.climbingnav.community.Repository.PostRepository;
import com.example.climbingnav.community.dto.comment.CommentSaveRequest;
import com.example.climbingnav.community.entity.Comment;
import com.example.climbingnav.community.entity.Post;
import com.example.climbingnav.community.entity.constants.StatusType;
import com.example.climbingnav.global.base.UserVo;
import com.example.climbingnav.global.base.types.ResponseCode;
import com.example.climbingnav.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Long saveComment(CommentSaveRequest commentSaveRequest, UserVo userVo) {
        Post post = postRepository.findById(commentSaveRequest.postId())
                .orElseThrow(() -> new CustomException(ResponseCode.BAD_REQUEST, "존재하지 않는 게시글입니다."));

        User user = userRepository.findById(userVo.userId())
                .orElseThrow(() -> new CustomException(ResponseCode.UNAUTHORIZED, "로그인이 필요합니다."));

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(commentSaveRequest.content())
                .status(StatusType.ACTIVE)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return savedComment.getId();
    }

    public String updateCommentStatusToDelete(Long commentId, UserVo userVo) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND, "존재하지 않는 댓글입니다."));

        if(!comment.getUser().getEmail().equals(userVo.email())) {
            throw new CustomException(ResponseCode.FORBIDDEN, "해당 댓글 작성자만 삭제가 가능합니다.");
        }

        if(comment.getStatus() == StatusType.DELETED) {
            throw new CustomException(ResponseCode.NOT_FOUND, "해당 댓글은 이미 삭제되었습니다.");
        }

        comment.changeStatus(StatusType.DELETED);

        return "success";
    }
}
