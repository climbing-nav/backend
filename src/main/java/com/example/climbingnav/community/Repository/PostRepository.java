package com.example.climbingnav.community.Repository;

import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.community.entity.Post;
import com.example.climbingnav.community.entity.constants.StatusType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("""
    SELECT p FROM Post p
    WHERE (:code IS NULL OR p.category.code = :code)
        AND (p.status = com.example.climbingnav.community.entity.constants.StatusType.ACTIVE)
      AND (:cursorId IS NULL OR p.id < :cursorId)
    ORDER BY p.id DESC
    """)
    List<Post> findActivePostsByCategory(
            String code,
            Long cursorId,
            Pageable pageable
    );

    Optional<Post> findByIdAndStatus(Long postId, StatusType status);

    @Query("""
    SELECT p FROM Post p
    WHERE p.user.id = :userId
      AND (:code IS NULL OR p.category.code = :code)
      AND p.status = com.example.climbingnav.community.entity.constants.StatusType.ACTIVE
      AND (:cursorId IS NULL OR p.id < :cursorId)
    ORDER BY p.id DESC
    """)
    List<Post> findMyActivePostsByCategory(
            Long userId,
            String code,
            Long cursorId,
            Pageable pageable
    );

}
