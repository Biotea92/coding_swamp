package com.study.codingswamp.study.service;

import com.study.codingswamp.auth.service.MemberPayload;
import com.study.codingswamp.common.exception.NotFoundException;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import com.study.codingswamp.study.domain.Study;
import com.study.codingswamp.study.domain.StudyStatus;
import com.study.codingswamp.study.domain.StudyType;
import com.study.codingswamp.study.service.request.StudyCreateRequest;
import com.study.codingswamp.study.service.response.StudyDetailResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class StudyServiceTest {

    @Autowired
    private StudyService studyService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clear() {
        jdbcTemplate.update("alter table member auto_increment= ?", 1);
        jdbcTemplate.update("alter table study auto_increment= ?", 1);
    }

    @Test
    @DisplayName("스터디 생성된다.")
    void createStudy() {
        // given
        Member member = createMember();
        MemberPayload memberPayload = new MemberPayload(member.getId(), member.getRole());
        StudyCreateRequest request = getStudyCreateRequest();

        // when
        Study study = studyService.createStudy(memberPayload, request);

        // then
        assertThat(study.getTitle()).isEqualTo(request.getTitle());
        assertThat(study.getDescription()).isEqualTo(request.getDescription());
        assertThat(study.getStudyType()).isEqualTo(StudyType.STUDY);
        assertThat(study.getThumbnail()).isEqualTo(request.getThumbnail());
        assertThat(study.getStudyStatus()).isEqualTo(StudyStatus.PREPARING);
        assertThat(study.getStartDate()).isEqualTo(LocalDate.of(2023, 2, 1));
        assertThat(study.getEndDate()).isEqualTo(LocalDate.of(2023, 2, 3));
        assertThat(study.getOwnerId()).isEqualTo(member.getId());
        assertThat(study.getCurrentMemberCount()).isEqualTo(1);
        assertThat(study.getApplicants().size()).isEqualTo(0);
        assertThat(study.getTags().get(0).getTagText()).isEqualTo("태그1");
        assertThat(study.getTags().get(1).getTagText()).isEqualTo("태그2");
    }

    @Test
    @DisplayName("스터디 상세 단건 가져오기")
    void getStudyDetail() {
        // given
        Member member = createMember();
        MemberPayload memberPayload = new MemberPayload(member.getId(), member.getRole());
        StudyCreateRequest request = getStudyCreateRequest();
        Study study = studyService.createStudy(memberPayload, request);

        // when
        StudyDetailResponse response = studyService.getStudyDetails(study.getId());

        // then
        assertThat(response.getStudyId()).isEqualTo(study.getId());
        assertThat(response.getTitle()).isEqualTo(study.getTitle());
        assertThat(response.getDescription()).isEqualTo(study.getDescription());
        assertThat(response.getStudyType()).isEqualTo(study.getStudyType().name());
        assertThat(response.getThumbnail()).isEqualTo(study.getThumbnail());
        assertThat(response.getCurrentMemberCount()).isEqualTo(study.getCurrentMemberCount());
        assertThat(response.getMaxMemberCount()).isEqualTo(study.getMaxMemberCount());
        assertThat(response.getStartDate()).isEqualTo(study.getStartDate());
        assertThat(response.getEndDate()).isEqualTo(study.getEndDate());
        assertThat(response.getOwner().getMemberId()).isEqualTo(study.getOwnerId());
        assertThat(response.getParticipants().size()).isEqualTo(study.getParticipants().size());
        assertThat(response.getApplicants().size()).isEqualTo(study.getApplicants().size());
        assertThat(response.getTags().size()).isEqualTo(study.getTags().size());
    }

    @Test
    @DisplayName("스터디 상세 단건 가져올시 스터디가 없는 경우")
    void getStudyDetailNotFoundStudy() {
        // given
        Member member = createMember();
        MemberPayload memberPayload = new MemberPayload(member.getId(), member.getRole());
        StudyCreateRequest request = getStudyCreateRequest();
        studyService.createStudy(memberPayload, request);

        // expected
        assertThrows(
                NotFoundException.class,
                () -> studyService.getStudyDetails(100L)
        );
    }

    private Member createMember() {
        Member member = new Member("abc@gmail.com", "1q2w3e4r!", "hong", null);
        memberRepository.save(member);
        return member;
    }

    private static StudyCreateRequest getStudyCreateRequest() {
        return StudyCreateRequest.builder()
                .title("제목입니다.")
                .description("설명입니다.")
                .studyType("STUDY")
                .thumbnail("#000000")
                .startDate(LocalDate.of(2023, 2, 1))
                .endDate(LocalDate.of(2023, 2, 3))
                .maxMemberCount(30)
                .tags(List.of("태그1", "태그2"))
                .build();
    }
}