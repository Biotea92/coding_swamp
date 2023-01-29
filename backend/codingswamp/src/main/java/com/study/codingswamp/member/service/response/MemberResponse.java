package com.study.codingswamp.member.service.response;

import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.Role;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
public class MemberResponse {
    private final Long id;
    private final String email;
    private final Long githubId;
    private final String username;
    private final String imageUrl;
    private final String profileUrl;
    private final Role role;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime joinedAt;

    public MemberResponse(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.githubId = member.getGithubId();
        this.username = member.getUsername();
        this.imageUrl = member.getImageUrl();
        this.profileUrl = member.getProfileUrl();
        this.role = member.getRole();
        this.joinedAt = member.getJoinedAt();
    }
}
