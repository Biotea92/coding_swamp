package com.study.codingswamp.study.domain;

import com.study.codingswamp.common.exception.NotFoundException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Study {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudyType studyType;

    @Column(nullable = false)
    private String thumbnail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudyStatus studyStatus;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    private Long ownerId;

    private int currentMemberCount;

    private int maxMemberCount;

    @ElementCollection
    @CollectionTable(name = "study_participant", joinColumns = @JoinColumn(name = "study_id"))
    private Set<Participant> participants = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "study_applicant", joinColumns = @JoinColumn(name = "study_id"))
    private Set<Applicant> applicants = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "study_tag", joinColumns = @JoinColumn(name = "study_id"))
    private List<Tag> tags = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Study(String title, String description, StudyType studyType, String thumbnail,
                 StudyStatus studyStatus, LocalDate startDate, LocalDate endDate, Long ownerId,
                 int currentMemberCount, int maxMemberCount, Set<Participant> participants,
                 Set<Applicant> applicants, List<Tag> tags) {
        this.title = title;
        this.description = description;
        this.studyType = studyType;
        this.thumbnail = thumbnail;
        this.studyStatus = studyStatus;
        this.startDate = startDate;
        this.endDate = endDate;
        this.ownerId = ownerId;
        this.currentMemberCount = currentMemberCount;
        this.maxMemberCount = maxMemberCount;
        this.participants = participants;
        this.applicants = applicants;
        this.tags = tags;
    }

    public LocalDate getOwnerParticipationDate() {
        return this.getParticipants().stream()
                .filter(p -> Objects.equals(p.getMemberId(), ownerId))
                .findAny()
                .orElseThrow(() -> new NotFoundException("participant", "owner에 해당되는 참가자가 없습니다."))
                .getParticipationDate();
    }

    public void addApplicant(Applicant applicant) {
        this.applicants.add(applicant);
    }
}
