package com.study.codingswamp.domain.study.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewResponse {

    private final ParticipantResponse participantResponse;
    private final Long reviewId;
    private final String content;
    private final LocalDateTime createdAt;

    @Builder
    public ReviewResponse(ParticipantResponse participantResponse, Long reviewId, String content, LocalDateTime createdAt) {
        this.participantResponse = participantResponse;
        this.reviewId = reviewId;
        this.content = content;
        this.createdAt = createdAt;
    }
}
