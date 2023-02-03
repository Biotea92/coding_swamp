package com.study.codingswamp.study.service.response;

import com.study.codingswamp.member.domain.Member;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ApplicantResponse {

    private final Long memberId;
    private final String username;
    private final String imageUrl;
    private final String profileUrl;
    private final LocalDate applicationDate;

    public ApplicantResponse(Member member, LocalDate applicationDate) {
        this.memberId = member.getId();
        this.username = member.getUsername();
        this.imageUrl = member.getImageUrl();
        this.profileUrl = member.getProfileUrl();
        this.applicationDate = applicationDate;
    }
}
