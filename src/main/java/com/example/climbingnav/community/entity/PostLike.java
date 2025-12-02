package com.example.climbingnav.community.entity;

import com.example.climbingnav.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Table(
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "post_id"})}
)
@Entity
public class PostLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;
}
