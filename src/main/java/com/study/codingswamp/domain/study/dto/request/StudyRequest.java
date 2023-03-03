package com.study.codingswamp.domain.study.dto.request;

import com.study.codingswamp.exception.InvalidRequestException;
import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.study.entity.Study;
import com.study.codingswamp.domain.study.entity.Tag;
import com.study.codingswamp.domain.study.entity.StudyStatus;
import com.study.codingswamp.domain.study.entity.StudyType;
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
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class StudyRequest {

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

    @Builder
    public StudyRequest(String title, String description, String studyType, String thumbnail,
                        LocalDate startDate, LocalDate endDate, Integer maxMemberCount, List<String> tags) {
        this.title = title;
        this.description = description;
        this.studyType = studyType;
        this.thumbnail = thumbnail;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxMemberCount = maxMemberCount;
        this.tags = tags;
    }

    public Study mapToStudy(Member owner) {
        return Study.builder()
                .title(this.title)
                .description(this.description)
                .studyType(mapToStudyType())
                .thumbnail(this.thumbnail)
                .studyStatus(checkStudyStatus())
                .startDate(this.startDate)
                .endDate(this.endDate)
                .owner(owner)
                .maxMemberCount(this.maxMemberCount)
                .applicants(new HashSet<>())
                .tags(mapToTag())
                .build();
    }

    public StudyType mapToStudyType() {
        if (this.studyType.equals("STUDY")) {
            return StudyType.STUDY;
        } else if (this.studyType.equals("MOGAKKO")) {
            return StudyType.MOGAKKO;
        }
        throw new InvalidRequestException("studyType", "STUDY 또는 MOGAKKO 이어야 합니다.");
    }

    public StudyStatus checkStudyStatus() {
        LocalDate now = LocalDate.now();
        if (endDate != null && endDate.isBefore(now)) {
            return StudyStatus.COMPLETION;
        }
        if (startDate.isAfter(now)) {
            return StudyStatus.PREPARING;
        }
        return StudyStatus.ONGOING;
    }

    public List<Tag> mapToTag() {
        return tags.stream()
                .map(Tag::new)
                .collect(Collectors.toList());
    }
}
