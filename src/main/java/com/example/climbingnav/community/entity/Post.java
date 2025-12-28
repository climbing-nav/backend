package com.example.climbingnav.community.entity;

import com.example.climbingnav.auth.entity.User;
import com.example.climbingnav.community.entity.constants.StatusType;
import com.example.climbingnav.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditOverride;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@AuditOverride(forClass = BaseEntity.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @Builder.Default
    private List<UploadFile> files = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusType status = StatusType.ACTIVE;

    private Long likeCount = 0L;

    public void changeStatus(StatusType statusType) {
        this.status = statusType;
    }

    public void update(String title, String content, Category newCategory) {
        if (title != null && !title.equals(this.title)) {
            this.title = title;
        }

        if (content != null && !content.equals(this.content)) {
            this.content = content;
        }

        if (newCategory != null && !this.category.getId().equals(newCategory.getId())) {
            this.category = newCategory;
        }
    }

    public void increaseLikeCount() {this.likeCount++;}
    public void decreaseLikeCount() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }
}
