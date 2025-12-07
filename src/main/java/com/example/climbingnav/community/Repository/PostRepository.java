package com.example.climbingnav.community.Repository;

import com.example.climbingnav.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findTop21ByOrderByIdDesc();

    List<Post> findTop21ByIdLessThanOrderByIdDesc(Long id);
}
