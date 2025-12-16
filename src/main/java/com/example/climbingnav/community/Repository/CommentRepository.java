package com.example.climbingnav.community.Repository;

import com.example.climbingnav.community.entity.Comment;
import com.example.climbingnav.community.entity.constants.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Modifying
    @Query("""
        update Comment c
            set c.status = :status
            where c.post.id = :postId
    """)
    void updateStatusByPostId(Long postId, StatusType statusType);
}
