package com.study.codingswamp.study.service.response;


import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.study.domain.Study;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class OwnerResponse {

    private final Long memberId;
    private final String username;
    private final String imageUrl;
    private final String profileUrl;
    private final LocalDate participationDate;

    public OwnerResponse(Study study, Member owner) {
        this.memberId = owner.getId();
        this.username = owner.getUsername();
        this.imageUrl = owner.getImageUrl();
        this.profileUrl = owner.getProfileUrl();
        this.participationDate = study.getOwnerParticipationDate();
    }
}
