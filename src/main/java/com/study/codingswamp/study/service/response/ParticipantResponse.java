package com.study.codingswamp.study.service.response;

import com.study.codingswamp.member.domain.Member;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ParticipantResponse {

    private final Long memberId;
    private final String username;
    private final String imageUrl;
    private final String profileUrl;
    private final LocalDate participationDate;

    public ParticipantResponse(Member member, LocalDate participationDate) {
        this.memberId = member.getId();
        this.username = member.getUsername();
        this.imageUrl = member.getImageUrl();
        this.profileUrl = member.getProfileUrl();
        this.participationDate = participationDate;
    }
}
