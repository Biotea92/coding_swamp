package com.study.codingswamp.study.service.request;

import com.study.codingswamp.common.exception.InvalidRequestException;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.study.domain.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudyCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String studyType;

    @NotBlank
    private String thumbnail;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Max(100)
    private Integer maxMemberCount;

    @NotNull
    @NotEmpty
    private List<String> tags;

    public Study mapToStudy(Member owner) {
        Set<Participant> participants = initParticipants(owner);
        return Study.builder()
                .title(this.title)
                .description(this.description)
                .studyType(validateStudyType())
                .thumbnail(this.thumbnail)
                .studyStatus(getStudyStatus())
                .startDate(this.startDate)
                .endDate(this.endDate)
                .owner(owner)
                .currentMemberCount(participants.size())
                .maxMemberCount(this.maxMemberCount)
                .participants(participants)
                .applicants(new HashSet<>())
                .tags(mapToTag())
                .build();
    }

    private Set<Participant> initParticipants(Member owner) {
        Set<Participant> participants = new HashSet<>();
        Participant participant = new Participant(owner, LocalDate.now());
        participants.add(participant);
        return participants;
    }

    private StudyType validateStudyType() {
        if (this.studyType.equals("STUDY")) {
            return StudyType.STUDY;
        } else if (this.studyType.equals("MOGAKKO")) {
            return StudyType.MOGAKKO;
        }
        throw new InvalidRequestException("studyType", "STUDY 또는 MOGAKKO 이어야 합니다.");
    }

    private StudyStatus getStudyStatus() {
        LocalDate now = LocalDate.now();
        if (endDate.isBefore(now)) {
            return StudyStatus.COMPLETION;
        }
        if (startDate.isAfter(now)) {
            return StudyStatus.PREPARING;
        }
        return StudyStatus.ONGOING;
    }

    private List<Tag> mapToTag() {
        return tags.stream()
                .map(Tag::new)
                .collect(Collectors.toList());
    }
}
