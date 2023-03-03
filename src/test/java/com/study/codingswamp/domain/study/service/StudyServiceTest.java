package com.study.codingswamp.domain.study.service;

import com.study.codingswamp.application.auth.MemberPayload;
import com.study.codingswamp.exception.ConflictException;
import com.study.codingswamp.exception.ForbiddenException;
import com.study.codingswamp.exception.NotFoundException;
import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.member.repository.MemberRepository;
import com.study.codingswamp.domain.study.entity.*;
import com.study.codingswamp.domain.study.service.StudyService;
import com.study.codingswamp.domain.study.repository.ApplicantRepository;
import com.study.codingswamp.domain.study.repository.ParticipantRepository;
import com.study.codingswamp.domain.study.repository.StudyRepository;
import com.study.codingswamp.domain.study.dto.request.ApplyRequest;
import com.study.codingswamp.domain.study.dto.request.SearchCondition;
import com.study.codingswamp.domain.study.dto.request.StudiesPageableRequest;
import com.study.codingswamp.domain.study.dto.request.StudyRequest;
import com.study.codingswamp.domain.study.dto.response.StudiesResponse;
import com.study.codingswamp.domain.study.dto.response.StudyDetailResponse;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        Member member = createMember();
        MemberPayload memberPayload = new MemberPayload(member.getId(), member.getRole());
        StudyRequest request = getStudyCreateRequest(30);

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
        StudyRequest request = getStudyCreateRequest(30);
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
        StudyRequest request = getStudyCreateRequest(30);
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
        StudyRequest studyRequest = getStudyCreateRequest(30);
        Study study = studyService.createStudy(memberPayload, studyRequest);

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
        StudyRequest studyRequest = getStudyCreateRequest(1);
        Study study = studyService.createStudy(memberPayload, studyRequest);

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
        StudyRequest studyRequest = getStudyCreateRequest(10);
        Study study = studyService.createStudy(memberPayload, studyRequest);

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
        StudyRequest studyRequest = getStudyCreateRequest(30);
        Study study = studyService.createStudy(memberPayload, studyRequest);

        ApplyRequest applyRequest = new ApplyRequest("지원 동기입니다.");

        // then
        assertThrows(
                ConflictException.class,
                () -> studyService.apply(memberPayload, study.getId(), applyRequest)
        );
    }

    @Test
    @DisplayName("스터디 신청 인원을 승인하면 참가자 인원이 된다.")
    void approve() {
        // given
        Member studyOwner = createMember();
        MemberPayload ownerPayload = new MemberPayload(studyOwner.getId(), studyOwner.getRole());
        StudyRequest studyRequest = getStudyCreateRequest(30);
        Study study = studyService.createStudy(ownerPayload, studyRequest);
        Member member = memberRepository.save(new Member("applicant@gmail.com", "testpassword", "kim", null));
        Applicant applicant = new Applicant(study, member, "지원동기", LocalDate.now());
        study.addApplicant(applicant);

        // when
        studyService.approve(ownerPayload, study.getId(), applicant.getMember().getId());


        // then
        assertThat(study.getApplicants()).isEmpty();
        assertThat(study.getParticipants().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("스터디 장이 아니면 신청인원을 승인할 수 없다.")
    void approveNotOwner() {
        // given
        Member studyOwner = createMember();
        MemberPayload ownerPayload = new MemberPayload(studyOwner.getId(), studyOwner.getRole());
        StudyRequest studyRequest = getStudyCreateRequest(30);
        Study study = studyService.createStudy(ownerPayload, studyRequest);
        Member member = memberRepository.save(new Member("applicant@gmail.com", "testpassword", "kim", null));
        Applicant applicant = new Applicant(study, member, "지원동기", LocalDate.now());
        study.addApplicant(applicant);
        MemberPayload memberPayload = new MemberPayload(member.getId(), member.getRole());

        // then
        assertThrows(
                ForbiddenException.class,
                () -> studyService.approve(memberPayload, study.getId(), applicant.getMember().getId())
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
        Member member = createMember();
        studies.forEach(study -> {
            Applicant applicant = new Applicant(study, member, "지원동기", LocalDate.now());
            applicantRepository.save(applicant);
            study.addApplicant(applicant);
        });
        MemberPayload memberPayload = new MemberPayload(member.getId(), member.getRole());

        // when
        StudiesResponse response = studyService.getMyApplies(memberPayload);

        // then
        assertThat(response.getStudyResponses().size()).isEqualTo(20);
    }

    @Test
    @DisplayName("본인이 참가한 스터디 목록만 가져오기")
    void getMyParticipates() {
        // given
        List<Study> studies = 이십개_스터디_만들기();
        Member member = createMember();
        studies.forEach(study -> {
            Applicant applicant = new Applicant(study, member, "지원동기", LocalDate.now());
            applicantRepository.save(applicant);
            study.addApplicant(applicant);
        });
        studies.forEach(study -> {
            Participant participant = new Participant(study, member, LocalDate.now());
            participantRepository.save(participant);
            study.addParticipant(participant);
        });
        MemberPayload memberPayload = new MemberPayload(member.getId(), member.getRole());

        // when
        StudiesResponse response = studyService.getMyParticipates(memberPayload);

        // then
        assertThat(response.getStudyResponses().size()).isEqualTo(20);
    }

    @Test
    @DisplayName("스터디 게시물 수정")
    void edit() {
        // given
        Member member = createMember();
        MemberPayload memberPayload = new MemberPayload(member.getId(), member.getRole());
        Study study = studyService.createStudy(memberPayload, getStudyCreateRequest(30));

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
        studyService.edit(memberPayload, study.getId(), request);

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
        Member studyOwner = createMember();
        MemberPayload memberPayload = new MemberPayload(studyOwner.getId(), studyOwner.getRole());
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
        studyService.delete(memberPayload, study.getId());

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
        Member studyOwner = createMember();
        Member member = createMember();
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
        study.initParticipants(new Participant(study, studyOwner, now));
        study.initParticipants(new Participant(study, member, now));
        studyRepository.save(study);
        MemberPayload memberPayload = new MemberPayload(member.getId(), member.getRole());

        // when
        studyService.withdraw(memberPayload, study.getId());

        // then
        assertEquals(1, study.getParticipants().size());
        assertFalse(study.getParticipants().contains(new Participant(study, member, now)));
        assertTrue(study.getParticipants().contains(new Participant(study, studyOwner, now)));
    }

    @Test
    @DisplayName("스터디장은 참가자를 kick 할 수 있다.")
    void kick() {
        // given
        Member studyOwner = createMember();
        Member member = createMember();
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
        Participant ownerParticipant = new Participant(study, studyOwner, now);
        Participant participant = new Participant(study, member, now);
        study.initParticipants(ownerParticipant);
        study.initParticipants(participant);
        studyRepository.save(study);
        MemberPayload memberPayload = new MemberPayload(studyOwner.getId(), studyOwner.getRole());

        // when
        studyService.kickParticipant(memberPayload, study.getId(), member.getId());

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

    private List<Study> 이십개_스터디_만들기() {
        Member studyOwner = createMember();
        List<Study> studies = IntStream.range(0, 20)
                .mapToObj(i -> {
                    Study study = Study.builder()
                                    .title("제목입니다. " + i)
                                    .description("설명입니다. " + i)
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
                    study.initParticipants(new Participant(study, studyOwner, LocalDate.now()));
                    return study;
                })
                .collect(Collectors.toList());
        return studyRepository.saveAll(studies);
    }

    @Test
    @DisplayName("스터디 신청을 취소할 수 있다.")
    void cancelApply() {
        // given
        Member studyOwner = createMember();
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
        Member applicantMember = createMember();
        Applicant applicant = new Applicant(study, applicantMember, "지원동기", LocalDate.now());
        study.addApplicant(applicant);
        applicantRepository.save(applicant);
        MemberPayload memberPayload = new MemberPayload(applicantMember.getId(), applicantMember.getRole());

        // when
        studyService.cancelApply(memberPayload, study.getId());

        // then
        assertThat(study.getApplicants().size()).isEqualTo(0);
    }

    private Member createMember() {
        Member member = new Member("abc@gmail.com", "1q2w3e4r!", "hong", null);
        memberRepository.save(member);
        return member;
    }

    private StudyRequest getStudyCreateRequest(int maxMemberCount) {
        return StudyRequest.builder()
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