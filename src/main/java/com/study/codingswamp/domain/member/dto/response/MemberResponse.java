package com.study.codingswamp.domain.member.dto.response;

import com.study.codingswamp.domain.member.entity.Role;
import com.study.codingswamp.domain.member.entity.Member;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
public class MemberResponse {
    private final Long memberId;
    private final String email;
    private final Long githubId;
    private final String username;
    private final String imageUrl;
    private final String profileUrl;
    private final Role role;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime joinedAt;

    public MemberResponse(Member member) {
        this.memberId = member.getId();
        this.email = member.getEmail();
        this.githubId = member.getGithubId();
        this.username = member.getUsername();
        this.imageUrl = member.getImageUrl();
        this.profileUrl = member.getProfileUrl();
        this.role = member.getRole();
        this.joinedAt = member.getJoinedAt();
    }
}
