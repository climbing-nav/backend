package com.example.climbingnav.community.Repository;

import com.example.climbingnav.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
