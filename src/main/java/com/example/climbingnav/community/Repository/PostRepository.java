package com.example.climbingnav.community.Repository;

import com.example.climbingnav.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
