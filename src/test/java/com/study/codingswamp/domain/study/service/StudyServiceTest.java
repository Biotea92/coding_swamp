package com.study.codingswamp.domain.study.service;

import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.member.repository.MemberRepository;
import com.study.codingswamp.domain.study.dto.request.ApplyRequest;
import com.study.codingswamp.domain.study.dto.request.SearchCondition;
import com.study.codingswamp.domain.study.dto.request.StudiesPageableRequest;
import com.study.codingswamp.domain.study.dto.request.StudyRequest;
import com.study.codingswamp.domain.study.dto.response.StudiesResponse;
import com.study.codingswamp.domain.study.dto.response.StudyDetailResponse;
import com.study.codingswamp.domain.study.entity.*;
import com.study.codingswamp.domain.study.repository.ApplicantRepository;
import com.study.codingswamp.domain.study.repository.ParticipantRepository;
import com.study.codingswamp.domain.study.repository.StudyRepository;
import com.study.codingswamp.exception.ConflictException;
import com.study.codingswamp.exception.ForbiddenException;
import com.study.codingswamp.exception.NotFoundException;
import com.study.codingswamp.util.fixture.dto.study.ApplyRequestFixture;
import com.study.codingswamp.util.fixture.dto.study.StudyRequestFixture;
import com.study.codingswamp.util.fixture.entity.member.MemberFixture;
import com.study.codingswamp.util.fixture.entity.study.ApplicantFixture;
import com.study.codingswamp.util.fixture.entity.study.ParticipantFixture;
import com.study.codingswamp.util.fixture.entity.study.StudyFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class StudyServiceTest {

    @Autowired
    private StudyService studyService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private StudyRepository studyRepository;
    @Autowired
    private ApplicantRepository applicantRepository;
    @Autowired
    private ParticipantRepository participantRepository;

    @BeforeEach
    void clear() {
        jdbcTemplate.update("alter table study auto_increment= ?", 1);
    }

    @Test
    @DisplayName("스터디 생성된다.")
    void createStudy() {
        // given
        Member member = memberRepository.save(MemberFixture.create(true));

        StudyRequest request = StudyRequestFixture.create();

        // when
        Study study = studyService.createStudy(member.getId(), request);

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
        Member member = memberRepository.save(MemberFixture.create(true));
        StudyRequest request = StudyRequestFixture.create();
        Study study = studyService.createStudy(member.getId(), request);

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
        Member studyOwner = memberRepository.save(MemberFixture.create(true));
        StudyRequest studyRequest = StudyRequestFixture.create();
        Study study = studyService.createStudy(studyOwner.getId(), studyRequest);

        Member applicantMember = memberRepository.save(MemberFixture.createGithubMember());
        ApplyRequest applyRequest = ApplyRequestFixture.create();

        // when
        studyService.apply(applicantMember.getId(), study.getId(), applyRequest);

        // then
        assertThat(study.getApplicants().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("최대 정원인 스터디에 지원할 경우 error가 발생한다.")
    void applyWhenMaxMemberCount() {
        // given
        Member studyOwner = memberRepository.save(MemberFixture.create(true));;
        StudyRequest studyRequest = StudyRequestFixture.create(1);
        Study study = studyService.createStudy(studyOwner.getId(), studyRequest);

        Member applicantMember = memberRepository.save(MemberFixture.createGithubMember());;
        ApplyRequest applyRequest = ApplyRequestFixture.create();

        // expected
        assertThrows(
                ConflictException.class,
                () -> studyService.apply(applicantMember.getId(), study.getId(), applyRequest)
        );
    }

    @Test
    @DisplayName("같은 곳에 두 번 지원하는 경우 error를 발생한다.")
    void applyTwice() {
        // given
        Member studyOwner = memberRepository.save(MemberFixture.create(true));
        StudyRequest studyRequest = StudyRequestFixture.create();
        Study study = studyService.createStudy(studyOwner.getId(), studyRequest);

        Member applicantMember = memberRepository.save(MemberFixture.createGithubMember());
        ApplyRequest applyRequest = ApplyRequestFixture.create();
        studyService.apply(applicantMember.getId(), study.getId(), applyRequest);

        // expected
        assertThrows(
                ConflictException.class,
                () -> studyService.apply(applicantMember.getId(), study.getId(), applyRequest)
        );
    }

    @Test
    @DisplayName("스터디 참가 신청시 이미 스터디원이면 예외가 발생한다.")
    void applyIfParticipant() {
        // given
        Member studyOwner = memberRepository.save(MemberFixture.create(true));
        StudyRequest studyRequest = StudyRequestFixture.create();
        Study study = studyService.createStudy(studyOwner.getId(), studyRequest);

        ApplyRequest applyRequest = ApplyRequestFixture.create();

        // then
        assertThrows(
                ConflictException.class,
                () -> studyService.apply(studyOwner.getId(), study.getId(), applyRequest)
        );
    }

    @Test
    @DisplayName("스터디 신청 인원을 승인하면 참가자 인원이 된다.")
    void approve() {
        // given
        Member studyOwner = memberRepository.save(MemberFixture.create(true));
        StudyRequest studyRequest = StudyRequestFixture.create();
        Study study = studyService.createStudy(studyOwner.getId(), studyRequest);
        Member member = memberRepository.save(MemberFixture.createGithubMember());
        Applicant applicant = ApplicantFixture.create(study, member);
        study.addApplicant(applicant);

        // when
        studyService.approve(studyOwner.getId(), study.getId(), applicant.getMember().getId());

        // then
        assertThat(study.getApplicants()).isEmpty();
        assertThat(study.getParticipants().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("스터디 장이 아니면 신청인원을 승인할 수 없다.")
    void approveNotOwner() {
        // given
        Member studyOwner = memberRepository.save(MemberFixture.create(true));
        StudyRequest studyRequest = StudyRequestFixture.create();
        Study study = studyService.createStudy(studyOwner.getId(), studyRequest);
        Member member = memberRepository.save(MemberFixture.createGithubMember());
        Applicant applicant = ApplicantFixture.create(study, member);
        study.addApplicant(applicant);

        // then
        assertThrows(
                ForbiddenException.class,
                () -> studyService.approve(member.getId(), study.getId(), applicant.getMember().getId())
        );
    }

    @Test
    @DisplayName("스터디 여러건 조회 1페이지")
    void getStudies() {
        // given
        이십개_스터디_만들기();

        StudiesPageableRequest studiesPageableRequest = new StudiesPageableRequest(1, 8);

        // when
        StudiesResponse response = studyService.getStudies(studiesPageableRequest);

        // then
        assertThat(3).isEqualTo(response.getTotalPage());
        assertThat(response.getStudyResponses().get(0).getTitle()).isEqualTo("제목입니다. 19");
        assertThat(response.getStudyResponses().get(7).getTitle()).isEqualTo("제목입니다. 12");
        assertThat(response.getStudyResponses().size()).isEqualTo(8);
    }

    @Test
    @DisplayName("본인이 신청한 스터디 목록만 가져오기")
    void getMyApplies() {
        // given
        List<Study> studies = 이십개_스터디_만들기();
        Member member = memberRepository.save(MemberFixture.create(true));
        studies.forEach(study -> {
            Applicant applicant = ApplicantFixture.create(study, member);
            applicantRepository.save(applicant);
            study.addApplicant(applicant);
        });

        // when
        StudiesResponse response = studyService.getMyApplies(member.getId());

        // then
        assertThat(response.getStudyResponses().size()).isEqualTo(20);
    }

    @Test
    @DisplayName("본인이 참가한 스터디 목록만 가져오기")
    void getMyParticipates() {
        // given
        List<Study> studies = 이십개_스터디_만들기();
        Member member = memberRepository.save(MemberFixture.create(true));;
        studies.forEach(study -> {
            Applicant applicant = ApplicantFixture.create(study, member);
            applicantRepository.save(applicant);
            study.addApplicant(applicant);
        });
        studies.forEach(study -> {
            Participant participant = ParticipantFixture.create(member, study);
            participantRepository.save(participant);
            study.addParticipant(participant);
        });

        // when
        StudiesResponse response = studyService.getMyParticipates(member.getId());

        // then
        assertThat(response.getStudyResponses().size()).isEqualTo(20);
    }

    @Test
    @DisplayName("스터디 게시물 수정")
    void edit() {
        // given
        Member member = memberRepository.save(MemberFixture.create(true));
        Study study = studyService.createStudy(member.getId(), StudyRequestFixture.create());

        StudyRequest request = StudyRequest.builder()
                .title("제목입니다. 수정")
                .description("설명입니다. 수정")
                .studyType("MOGAKKO")
                .thumbnail("#000001")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(3))
                .maxMemberCount(2)
                .tags(List.of("태그1 수정", "태그2 수정"))
                .build();

        // when
        studyService.edit(member.getId(), study.getId(), request);

        assertThat(study.getTitle()).isEqualTo("제목입니다. 수정");
        assertThat(study.getDescription()).isEqualTo("설명입니다. 수정");
        assertThat(study.getStudyType()).isEqualTo(StudyType.MOGAKKO);
        assertThat(study.getThumbnail()).isEqualTo("#000001");
        assertThat(study.getStartDate()).isEqualTo(LocalDate.now().plusDays(2));
        assertThat(study.getEndDate()).isEqualTo(LocalDate.now().plusDays(3));
        assertThat(study.getMaxMemberCount()).isEqualTo(2);
        assertThat(study.getTags().get(0).getTagText()).isEqualTo("태그1 수정");
    }

    @Test
    @DisplayName("스터디 장은 스터디를 삭제할 수 있다.")
    void delete() {
        // when
        Member studyOwner = memberRepository.save(MemberFixture.create(true));
        Study study = Study.builder()
                .title("제목입니다.")
                .description("설명입니다.")
                .studyStatus(StudyStatus.PREPARING)
                .studyType(StudyType.STUDY)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .owner(studyOwner)
                .currentMemberCount(1)
                .maxMemberCount(30)
                .thumbnail("#00000")
                .applicants(new HashSet<>())
                .participants(new HashSet<>())
                .tags(List.of(new Tag("태그1"), new Tag("태그2")))
                .build();
        studyRepository.save(study);

        // when
        studyService.delete(studyOwner.getId(), study.getId());

        // then
        assertThat(studyRepository.findById(study.getId())).isEmpty();
        assertThrows(
            RuntimeException.class,
            () -> studyRepository.findById(study.getId()).orElseThrow(RuntimeException::new)
        );
    }

    @Test
    @DisplayName("참가자는 스터디를 탈퇴할 수 있다.")
    void withdraw() {
        // given
        Member studyOwner = memberRepository.save(MemberFixture.create(true));;
        Member member = memberRepository.save(MemberFixture.createGithubMember());
        LocalDate now = LocalDate.now();
        Study study = Study.builder()
                .title("제목입니다.")
                .description("설명입니다.")
                .studyStatus(StudyStatus.PREPARING)
                .studyType(StudyType.STUDY)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .owner(studyOwner)
                .currentMemberCount(1)
                .maxMemberCount(30)
                .thumbnail("#00000")
                .applicants(new HashSet<>())
                .participants(new HashSet<>())
                .tags(List.of(new Tag("태그1"), new Tag("태그2")))
                .build();
        study.initParticipants(ParticipantFixture.create(studyOwner, study));
        study.initParticipants(ParticipantFixture.create(member, study));
        studyRepository.save(study);

        // when
        studyService.withdraw(member.getId(), study.getId());

        // then
        assertEquals(1, study.getParticipants().size());
        assertFalse(study.getParticipants().contains(new Participant(study, member, now)));
        assertTrue(study.getParticipants().contains(new Participant(study, studyOwner, now)));
    }

    @Test
    @DisplayName("스터디장은 참가자를 kick 할 수 있다.")
    void kick() {
        // given
        Member studyOwner = memberRepository.save(MemberFixture.create(true));
        Member member = memberRepository.save(MemberFixture.createGithubMember());
        Study study = Study.builder()
                .title("제목입니다.")
                .description("설명입니다.")
                .studyStatus(StudyStatus.PREPARING)
                .studyType(StudyType.STUDY)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .owner(studyOwner)
                .currentMemberCount(1)
                .maxMemberCount(30)
                .thumbnail("#00000")
                .applicants(new HashSet<>())
                .participants(new HashSet<>())
                .tags(List.of(new Tag("태그1"), new Tag("태그2")))
                .build();
        Participant ownerParticipant = ParticipantFixture.create(studyOwner, study);
        Participant participant = ParticipantFixture.create(member, study);
        study.initParticipants(ownerParticipant);
        study.initParticipants(participant);
        studyRepository.save(study);

        // when
        studyService.kickParticipant(studyOwner.getId(), study.getId(), member.getId());

        // then
        assertThat(study.getParticipants().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("스터디 Search 여러건 조회 1페이지")
    void getSearchStudies() {
        // given
        이십개_스터디_만들기();
        SearchCondition searchCondition = new SearchCondition(1, 8, "제목", "STUDY", "태그");

        // when
        StudiesResponse response = studyService.getSearchStudies(searchCondition);

        // then
        assertThat(3).isEqualTo(response.getTotalPage());
        assertThat(response.getStudyResponses().get(0).getTitle()).isEqualTo("제목입니다. 19");
        assertThat(response.getStudyResponses().get(7).getTitle()).isEqualTo("제목입니다. 12");
        assertThat(response.getStudyResponses().size()).isEqualTo(8);
    }

    @Test
    @DisplayName("스터디 신청을 취소할 수 있다.")
    void cancelApply() {
        // given
        Member studyOwner = memberRepository.save(MemberFixture.create(true));
        Study study = Study.builder()
                .title("제목입니다.")
                .description("설명입니다.")
                .studyStatus(StudyStatus.PREPARING)
                .studyType(StudyType.STUDY)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .owner(studyOwner)
                .currentMemberCount(1)
                .maxMemberCount(30)
                .thumbnail("#00000")
                .applicants(new HashSet<>())
                .participants(new HashSet<>())
                .tags(List.of(new Tag("태그1"), new Tag("태그2")))
                .build();
        studyRepository.save(study);
        Member applicantMember = memberRepository.save(MemberFixture.createGithubMember());
        Applicant applicant = ApplicantFixture.create(study, applicantMember);
        study.addApplicant(applicant);
        applicantRepository.save(applicant);

        // when
        studyService.cancelApply(applicantMember.getId(), study.getId());

        // then
        assertThat(study.getApplicants().size()).isEqualTo(0);
    }

    private List<Study> 이십개_스터디_만들기() {
        Member studyOwner = memberRepository.save(MemberFixture.create(true));
        List<Study> studies = StudyFixture.createStudies(studyOwner);
        return studyRepository.saveAll(studies);
    }
}