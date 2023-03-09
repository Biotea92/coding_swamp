package com.study.codingswamp.domain.member.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@SQLDelete(sql = "UPDATE member SET deleted = true WHERE member_id = ?")
//@Where(clause = "deleted = false")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String email;

    private String password;

    private Long githubId;

    private String username;

    private String imageUrl;

    private String profileUrl;

    @Enumerated(EnumType.STRING)
    private final Role role = Role.USER;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime joinedAt;

    private final boolean deleted = Boolean.FALSE;

    @PrePersist
    public void onPrePersist() {
        this.joinedAt = LocalDateTime.now();
    }

    @Builder
    public Member(String email, String password, String username, String imageUrl) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.imageUrl = imageUrl;
    }

    public Member(String email, Long githubId, String username, String imageUrl, String profileUrl) {
        this.email = email;
        this.githubId = githubId;
        this.username = username;
        this.imageUrl = imageUrl;
        this.profileUrl = profileUrl;
    }

    public void update(String username, String email, String imageUrl, String profileUrl) {
        this.email = email;
        this.username = username;
        this.imageUrl = imageUrl;
        this.profileUrl = profileUrl;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void updateUsername(String username) {
        this.username = username;
    }

    public void updateProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
}
