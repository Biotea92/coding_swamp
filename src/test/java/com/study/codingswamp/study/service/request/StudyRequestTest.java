package com.study.codingswamp.study.service.request;

import com.study.codingswamp.common.exception.InvalidRequestException;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.study.domain.Study;
import com.study.codingswamp.study.domain.StudyStatus;
import com.study.codingswamp.study.domain.StudyType;
import com.study.codingswamp.study.domain.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StudyRequestTest {

    @Test
    @DisplayName("StudyRequest 객체를 Study 객체로 바꿔준다.")
    void mapToStudy() {
        // given
        StudyRequest request = StudyRequest.builder()
                .title("제목입니다.")
                .description("설명입니다.")
                .studyType("STUDY")
                .thumbnail("#000000")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .maxMemberCount(30)
                .tags(List.of("태그1", "태그2"))
                .build();

        // when
        Member owner = new Member("abc@gmail.com", "1q2w3e4r!", "hong", null);
        Study study = request.mapToStudy(owner);

        assertThat(study.getTitle()).isEqualTo(request.getTitle());
        assertThat(study.getDescription()).isEqualTo(request.getDescription());
        assertThat(study.getStudyType()).isEqualTo(StudyType.STUDY);
        assertThat(study.getThumbnail()).isEqualTo(request.getThumbnail());
        assertThat(study.getStudyStatus()).isEqualTo(StudyStatus.PREPARING);
        assertThat(study.getStartDate()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(study.getEndDate()).isEqualTo(LocalDate.now().plusDays(2));
        assertThat(study.getOwner()).isEqualTo(owner);
        assertThat(study.getCurrentMemberCount()).isEqualTo(1);
        assertThat(study.getApplicants().size()).isEqualTo(0);
        assertThat(study.getTags()).isEqualTo(List.of(new Tag("태그1"), new Tag("태그2")));
    }

    @Test
    @DisplayName("studyStatus는 시간에 따라 check되어야 한다.")
    void studyStatusCheck() {
        // given
        StudyRequest request = StudyRequest.builder()
                .title("제목입니다.")
                .description("설명입니다.")
                .studyType("STUDY")
                .thumbnail("#000000")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .maxMemberCount(30)
                .tags(List.of("태그1", "태그2"))
                .build();

        // when
        Member owner = new Member("abc@gmail.com", "1q2w3e4r!", "hong", null);
        Study study = request.mapToStudy(owner);

        // then
        assertThat(study.getStudyStatus()).isEqualTo(StudyStatus.ONGOING);
    }

    @Test
    @DisplayName("studyType은 validate되어야 한다.")
    void studyTypeValidate() {
        // given
        StudyRequest request = StudyRequest.builder()
                .title("제목입니다.")
                .description("설명입니다.")
                .studyType("validateTest")
                .thumbnail("#000000")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .maxMemberCount(30)
                .tags(List.of("태그1", "태그2"))
                .build();
        Member owner = new Member("abc@gmail.com", "1q2w3e4r!", "hong", null);

        // expected
        assertThrows(
                InvalidRequestException.class,
                () -> request.mapToStudy(owner)
        );
    }
}