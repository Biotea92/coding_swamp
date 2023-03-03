package com.study.codingswamp.domain.study.dto.response;

import com.study.codingswamp.domain.study.entity.Study;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class StudyDetailResponse {

    private final Long studyId;
    private final String title;
    private final String description;
    private final String studyType;
    private final String thumbnail;
    private final String studyStatus;
    private final Integer currentMemberCount;
    private final Integer maxMemberCount;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final OwnerResponse owner;
    private final List<ParticipantResponse> participants;
    private final List<ApplicantResponse> applicants;
    private final List<String> tags;
    private final LocalDateTime createdAt;

    @Builder
    public StudyDetailResponse(Study study, OwnerResponse owner, List<ParticipantResponse> participants,
                               List<ApplicantResponse> applicants, List<String> tags) {
        this.studyId = study.getId();
        this.title = study.getTitle();
        this.description = study.getDescription();
        this.studyType = study.getStudyType().name();
        this.thumbnail = study.getThumbnail();
        this.studyStatus = study.getStudyStatus().name();
        this.currentMemberCount = study.getCurrentMemberCount();
        this.maxMemberCount = study.getMaxMemberCount();
        this.startDate = study.getStartDate();
        this.endDate = study.getEndDate();
        this.owner = owner;
        this.participants = participants;
        this.applicants = applicants;
        this.tags = tags;
        this.createdAt = study.getCreatedAt();
    }
}
