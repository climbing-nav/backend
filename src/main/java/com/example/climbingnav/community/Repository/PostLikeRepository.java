package com.example.climbingnav.community.Repository;

import com.example.climbingnav.community.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByUser_IdAndPost_Id(Long userId, Long postId);

    Optional<PostLike> findByUser_IdAndPost_Id(Long userId, Long postId);
}
