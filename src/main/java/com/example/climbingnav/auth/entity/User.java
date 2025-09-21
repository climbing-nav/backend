package com.example.climbingnav.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditOverride;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@AuditOverride(forClass = BaseEntity.class)
@Table(name = "users")
@Entity
public class User extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;
    private Boolean emailVerified = false;

    @Column(length = 100)
    private String nickname;

    @Column(columnDefinition = "text")
    private String avatarUrl;
}
