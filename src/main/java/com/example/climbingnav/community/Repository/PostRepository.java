package com.example.climbingnav.community.Repository;

import com.example.climbingnav.community.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

//    @Query("""
//        SELECT p FROM Post  p
//            WHERE p.status = com.example.climbingnav.community.entity.constants.StatusType.ACTIVE
//                AND (:cursorId IS NULL OR p.id < :cursorId)
//            ORDER BY p.id DESC
//    """)
//    List<Post> findActivePosts(Long cursorId, Pageable pageable);

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
}
