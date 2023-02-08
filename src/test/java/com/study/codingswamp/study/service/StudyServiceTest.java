package com.study.codingswamp.study.service;

import com.study.codingswamp.auth.service.MemberPayload;
import com.study.codingswamp.common.exception.ConflictException;
import com.study.codingswamp.common.exception.NotFoundException;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import com.study.codingswamp.study.domain.Study;
import com.study.codingswamp.study.domain.StudyStatus;
import com.study.codingswamp.study.domain.StudyType;
import com.study.codingswamp.study.service.request.ApplyRequest;
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
        jdbcTemplate.update("alter table study auto_increment= ?", 1);
    }

    @Test
    @DisplayName("스터디 생성된다.")
    void createStudy() {
        // given
        Member member = createMember();
        MemberPayload memberPayload = new MemberPayload(member.getId(), member.getRole());
        StudyCreateRequest request = getStudyCreateRequest(30);

        // when
        Study study = studyService.createStudy(memberPayload, request);

        // then
        assertThat(study.getTitle()).isEqualTo(request.getTitle());
        assertThat(study.getDescription()).isEqualTo(request.getDescription());
        assertThat(study.getStudyType()).isEqualTo(StudyType.STUDY);
        assertThat(study.getThumbnail()).isEqualTo(request.getThumbnail());
        assertThat(study.getStudyStatus()).isEqualTo(StudyStatus.PREPARING);
        assertThat(study.getStartDate()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(study.getEndDate()).isEqualTo(LocalDate.now().plusDays(2));
        assertThat(study.getOwner()).isEqualTo(member);
        assertThat(study.getCurrentMemberCount()).isEqualTo(1);
        assertThat(study.getApplicants().size()).isEqualTo(0);
        assertThat(study.getParticipants().size()).isEqualTo(1);
        assertThat(study.getTags().get(0).getTagText()).isEqualTo("태그1");
        assertThat(study.getTags().get(1).getTagText()).isEqualTo("태그2");
    }

    @Test
    @DisplayName("스터디 상세 단건 가져오기")
    void getStudyDetail() {
        // given
        Member member = createMember();
        MemberPayload memberPayload = new MemberPayload(member.getId(), member.getRole());
        StudyCreateRequest request = getStudyCreateRequest(30);
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
        assertThat(response.getOwner().getMemberId()).isEqualTo(study.getOwner().getId());
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
        StudyCreateRequest request = getStudyCreateRequest(30);
        studyService.createStudy(memberPayload, request);

        // expected
        assertThrows(
                NotFoundException.class,
                () -> studyService.getStudyDetails(100L)
        );
    }

    @Test
    @DisplayName("스터디 참가 신청시 스터디에 참가인원이 추가된다.")
    void apply() {
        // given
        Member studyOwner = createMember();
        MemberPayload memberPayload = new MemberPayload(studyOwner.getId(), studyOwner.getRole());
        StudyCreateRequest studyCreateRequest = getStudyCreateRequest(30);
        Study study = studyService.createStudy(memberPayload, studyCreateRequest);

        Member applicantMember = new Member("abc@gmail.com", "1q2w3e4r!", "kim", null);
        memberRepository.save(applicantMember);
        MemberPayload applicantMemberPayload = new MemberPayload(applicantMember.getId(), applicantMember.getRole());
        ApplyRequest applyRequest = new ApplyRequest("지원 동기입니다.");

        // when
        studyService.apply(applicantMemberPayload, study.getId(), applyRequest);

        // then
        assertThat(study.getApplicants().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("최대 정원인 스터디에 지원할 경우 error가 발생한다.")
    void applyWhenMaxMemberCount() {
        // given
        Member studyOwner = createMember();
        MemberPayload memberPayload = new MemberPayload(studyOwner.getId(), studyOwner.getRole());
        StudyCreateRequest studyCreateRequest = getStudyCreateRequest(1);
        Study study = studyService.createStudy(memberPayload, studyCreateRequest);

        Member applicantMember = new Member("abc@gmail.com", "1q2w3e4r!", "kim", null);
        memberRepository.save(applicantMember);
        MemberPayload applicantMemberPayload = new MemberPayload(applicantMember.getId(), applicantMember.getRole());
        ApplyRequest applyRequest = new ApplyRequest("지원 동기입니다.");

        // expected
        assertThrows(
                ConflictException.class,
                () -> studyService.apply(applicantMemberPayload, study.getId(), applyRequest)
        );
    }

    @Test
    @DisplayName("같은 곳에 두 번 지원하는 경우 error를 발생한다.")
    void applyTwice() {
        // given
        Member studyOwner = createMember();
        MemberPayload memberPayload = new MemberPayload(studyOwner.getId(), studyOwner.getRole());
        StudyCreateRequest studyCreateRequest = getStudyCreateRequest(10);
        Study study = studyService.createStudy(memberPayload, studyCreateRequest);

        Member applicantMember = new Member("abc@gmail.com", "1q2w3e4r!", "kim", null);
        memberRepository.save(applicantMember);
        MemberPayload applicantMemberPayload = new MemberPayload(applicantMember.getId(), applicantMember.getRole());
        ApplyRequest applyRequest = new ApplyRequest("지원 동기입니다.");
        studyService.apply(applicantMemberPayload, study.getId(), applyRequest);

        // expected
        assertThrows(
                ConflictException.class,
                () -> studyService.apply(applicantMemberPayload, study.getId(), applyRequest)
        );
    }

    @Test
    @DisplayName("스터디 참가 신청시 이미 스터디원이면 예외가 발생한다.")
    void applyIfParticipant() {
        // given
        Member studyOwner = createMember();
        MemberPayload memberPayload = new MemberPayload(studyOwner.getId(), studyOwner.getRole());
        StudyCreateRequest studyCreateRequest = getStudyCreateRequest(30);
        Study study = studyService.createStudy(memberPayload, studyCreateRequest);

        ApplyRequest applyRequest = new ApplyRequest("지원 동기입니다.");

        // then
        assertThrows(
                ConflictException.class,
                () -> studyService.apply(memberPayload, study.getId(), applyRequest)
        );
    }

    private Member createMember() {
        Member member = new Member("abc@gmail.com", "1q2w3e4r!", "hong", null);
        memberRepository.save(member);
        return member;
    }

    private StudyCreateRequest getStudyCreateRequest(int maxMemberCount) {
        return StudyCreateRequest.builder()
                .title("제목입니다.")
                .description("설명입니다.")
                .studyType("STUDY")
                .thumbnail("#000000")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .maxMemberCount(maxMemberCount)
                .tags(List.of("태그1", "태그2"))
                .build();
    }
}