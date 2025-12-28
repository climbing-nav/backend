package com.example.climbingnav.community.entity;

import com.example.climbingnav.community.entity.constants.StatusType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class UploadFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalName;
    private String url;
    private String s3Key;
    private Long size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusType status = StatusType.ACTIVE;

    public  void setPost(Post post) {
        this.post = post;
    }

    public void changeStatus(StatusType statusType) {
        this.status = statusType;
    }

}
