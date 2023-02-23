package com.study.codingswamp.study.domain;

import com.study.codingswamp.common.exception.ConflictException;
import com.study.codingswamp.common.exception.ForbiddenException;
import com.study.codingswamp.common.exception.NotFoundException;
import com.study.codingswamp.common.exception.UnauthorizedException;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.study.service.request.StudyRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static javax.persistence.FetchType.LAZY;
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

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "owner_id")
    private Member owner;

    private int currentMemberCount;

    private int maxMemberCount;

    @OneToMany(mappedBy = "study", fetch = LAZY)
    private Set<Participant> participants = new HashSet<>();

    @OneToMany(mappedBy = "study", fetch = LAZY)
    private Set<Applicant> applicants = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "study_tag",
            joinColumns = @JoinColumn(name = "study_id")
    )
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
                 StudyStatus studyStatus, LocalDate startDate, LocalDate endDate, Member owner,
                 int currentMemberCount, int maxMemberCount, Set<Participant> participants,
                 Set<Applicant> applicants, List<Tag> tags) {
        this.title = title;
        this.description = description;
        this.studyType = studyType;
        this.thumbnail = thumbnail;
        this.studyStatus = studyStatus;
        this.startDate = startDate;
        this.endDate = endDate;
        this.owner = owner;
        this.currentMemberCount = currentMemberCount;
        this.maxMemberCount = maxMemberCount;
        this.participants = new HashSet<>();
        this.applicants = applicants;
        this.tags = tags;
    }

    public void initParticipants(Participant participant) {
        participants.add(participant);
        currentMemberCount = participants.size();
    }

    public LocalDate getOwnerParticipationDate() {
        return participants.stream()
                .filter(p -> p.getMember() == owner)
                .findAny()
                .orElseThrow(() -> new NotFoundException("participant", "owner에 해당되는 참가자가 없습니다."))
                .getParticipationDate();
    }

    public void addApplicant(Applicant applicant) {
        this.applicants.add(applicant);
    }

    public Applicant addParticipant(Participant participant) {
        this.participants.add(participant);
        this.currentMemberCount = participants.size();
        Applicant removeApplicant = applicants.stream()
                .filter(applicant -> participant.getMember() == applicant.getMember())
                .findAny()
                .orElseThrow(() -> new NotFoundException("member", "신청자에 없습니다."));
        this.applicants.remove(removeApplicant);
        return removeApplicant;
    }

    public void validateOwner(Member member) {
        if (this.owner != member) {
            throw new ForbiddenException("owner", "스터디 장이 아닙니다.");
        }
    }

    public void validateStudyMaxMember() {
        if (this.currentMemberCount == maxMemberCount) {
            throw new ConflictException("study", "최대 정원인 스터디입니다.");
        }
    }

    public void checkApplicant(Member member) {
        Optional<Applicant> findApplicant = applicants.stream()
                .filter(applicant -> applicant.getMember().getId().equals(member.getId()))
                .findAny();
        if (findApplicant.isPresent()) {
            throw new ConflictException("applicant", "이미 신청한 사용자입니다.");
        }
    }

    public void checkParticipant(Member member) {
        Optional<Participant> findParticipant = participants.stream()
                .filter(participant -> participant.getMember() == member)
                .findAny();
        if (findParticipant.isPresent()) {
            throw new ConflictException("participant", "이미 참가한 인원입니다.");
        }
    }

    public Participant checkWithDrawParticipant(Member member) {
        if (member == owner) {
            throw new UnauthorizedException("owner", "스터디장은 탈퇴할 수 없습니다.");
        }
        Participant removeParticipant = findParticipant(member);
        this.participants.remove(removeParticipant);
        return removeParticipant;
    }

    public void update(StudyRequest request) {
        this.title = request.getTitle();
        this.description = request.getDescription();
        this.studyType = request.mapToStudyType();
        this.thumbnail = request.getThumbnail();
        this.startDate = request.getStartDate();
        this.endDate = request.getEndDate();
        this.studyStatus = request.checkStudyStatus();
        validateMaxMemberCount(request);
        this.maxMemberCount = request.getMaxMemberCount();
        this.tags = request.mapToTag();
    }

    private void validateMaxMemberCount(StudyRequest request) {
        if (currentMemberCount > request.getMaxMemberCount()) {
            throw new ConflictException("maxMemberCount", "현재 인원이 정원보다 많습니다.");
        }
    }

    public void updateStudyStatus(LocalDate now) {
        if (endDate != null && endDate.isBefore(now)) {
            this.studyStatus = StudyStatus.COMPLETION;
        } else if (startDate.isAfter(now)) {
            this.studyStatus = StudyStatus.PREPARING;
        } else {
            this.studyStatus = StudyStatus.ONGOING;
        }
    }

    public Participant kickParticipant(Member participantMember) {
        Participant removeParticipant = findParticipant(participantMember);
        this.participants.remove(removeParticipant);
        return removeParticipant;
    }

    public Applicant removeApplicant(Member member) {
        Applicant removeApplicant = applicants.stream()
                .filter(applicant -> member == applicant.getMember())
                .findAny()
                .orElseThrow(() -> new NotFoundException("member", "신청자에 없습니다."));
        this.applicants.remove(removeApplicant);
        return removeApplicant;
    }

    private Participant findParticipant(Member member) {
        return participants.stream()
                .filter(participant -> participant.getMember() == member)
                .findAny()
                .orElseThrow(() -> new NotFoundException("participant", "참가자가 아닙니다."));
    }
}
