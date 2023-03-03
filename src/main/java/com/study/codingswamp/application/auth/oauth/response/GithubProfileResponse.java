package com.study.codingswamp.application.auth.oauth.response;

import com.study.codingswamp.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class GithubProfileResponse {

    private final String githubId;
    private final String email;
    private final String username;
    private final String imageUrl;
    private final String profileUrl;

    @Builder
    public GithubProfileResponse(String githubId, String email, String username, String imageUrl, String profileUrl) {
        this.githubId = githubId;
        this.email = email;
        this.username = username;
        this.imageUrl = imageUrl;
        this.profileUrl = profileUrl;
    }

    public Member toMember() {
        return new Member(email, Long.valueOf(githubId), username, imageUrl, profileUrl);
    }
}
