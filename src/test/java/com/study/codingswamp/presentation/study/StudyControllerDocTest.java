package com.study.codingswamp.presentation.study;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.codingswamp.application.auth.token.TokenProvider;
import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.member.repository.MemberRepository;
import com.study.codingswamp.domain.study.dto.request.ApplyRequest;
import com.study.codingswamp.domain.study.dto.request.StudyRequest;
import com.study.codingswamp.domain.study.entity.*;
import com.study.codingswamp.domain.study.repository.ApplicantRepository;
import com.study.codingswamp.domain.study.repository.ParticipantRepository;
import com.study.codingswamp.domain.study.repository.StudyRepository;
import com.study.codingswamp.util.fixture.dto.study.ApplyRequestFixture;
import com.study.codingswamp.util.fixture.entity.member.MemberFixture;
import com.study.codingswamp.util.fixture.entity.study.ApplicantFixture;
import com.study.codingswamp.util.fixture.entity.study.StudyFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.removeHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@Transactional
public class StudyControllerDocTest {

    @Autowired
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private StudyRepository studyRepository;
    @Autowired
    private ApplicantRepository applicantRepository;
    @Autowired
    private ParticipantRepository participantRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint(), removeHeaders("Vary"))
                )
                .build();

        jdbcTemplate.update("alter table study auto_increment= ?", 1);
        jdbcTemplate.update("alter table member auto_increment= ?", 1);
    }

    @Test
    @DisplayName("스터디 생성 요청시 스터디가 생성되어야한다.")
    void createStudy() throws Exception {
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

        Member member = memberRepository.save(MemberFixture.create(true));
        String token = tokenProvider.createAccessToken(member.getId(), member.getRole());

        String json = objectMapper.writeValueAsString(request);

        // expected
        mockMvc.perform(post("/api/study")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .header(AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isCreated())
                .andDo(document("study-create",
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("Bearer auth credentials")
                        ),
                        requestFields(
                                fieldWithPath("title").description("제목"),
                                fieldWithPath("description").description("설명"),
                                fieldWithPath("studyType").description("스터디 타입 STUDY or MOGAKKO"),
                                fieldWithPath("thumbnail").description("썸네일 색상코드"),
                                fieldWithPath("startDate").description("스터디 시작일 포멧 (yy-MM-dd)"),
                                fieldWithPath("endDate").description("스터디 종료일 포멧 (yy-MM-dd)"),
                                fieldWithPath("maxMemberCount").description("스터디 최대인원"),
                                fieldWithPath("tags").description("태그 type(List)")
                        )
                ));
    }

    @Test
    @DisplayName("스터디 상세 단건 조회하기")
    void getStudyDetail() throws Exception {
        // given
        Member member = memberRepository.save(MemberFixture.create(true));
        Study study = StudyFixture.create(member);
        Participant participant = new Participant(study, member, LocalDate.now());
        study.initParticipants(participant);
        Member hong = memberRepository.save(MemberFixture.createGithubMember());
        study.addApplicant(ApplicantFixture.create(study, hong));
        studyRepository.save(study);

        // expected
        mockMvc.perform(get("/api/study/{studyId}", study.getId()))
                .andExpect(status().isOk())
                .andDo(document("study-get-detail",
                        pathParameters(
                               parameterWithName("studyId").description("스터디 아이디 type(Long)")
                        ),
                        responseFields(
                                fieldWithPath("studyId").description("스터디 아이디"),
                                fieldWithPath("title").description("제목"),
                                fieldWithPath("description").description("설명"),
                                fieldWithPath("studyType").description("스터디 타입"),
                                fieldWithPath("thumbnail").description("썸네일 색상코드"),
                                fieldWithPath("studyStatus").description("스터디 상태"),
                                fieldWithPath("currentMemberCount").description("현재 인원"),
                                fieldWithPath("maxMemberCount").description("최대 인원"),
                                fieldWithPath("startDate").description("스터디 시작일"),
                                fieldWithPath("endDate").description("스터디 종료일"),
                                fieldWithPath("owner").description("스터디장 정보"),
                                fieldWithPath("owner.memberId").description("스터디장 memberId"),
                                fieldWithPath("owner.username").description("스터디장 닉네임"),
                                fieldWithPath("owner.imageUrl").description("스터디장 이미지"),
                                fieldWithPath("owner.profileUrl").description("스터디장 깃헙주소"),
                                fieldWithPath("owner.participationDate").description("스터디장 참가일"),
                                fieldWithPath("participants").description("참가자 정보"),
                                fieldWithPath("participants[].memberId").description("참가자 memberId"),
                                fieldWithPath("participants[].username").description("참가자 닉네임"),
                                fieldWithPath("participants[].imageUrl").description("참가자 이미지"),
                                fieldWithPath("participants[].profileUrl").description("참가자 깃헙주소"),
                                fieldWithPath("participants[].participationDate").description("참가자 참가일"),
                                fieldWithPath("applicants").description("신청자 정보"),
                                fieldWithPath("applicants[].memberId").description("신청자 memberId"),
                                fieldWithPath("applicants[].username").description("신청자 닉네임"),
                                fieldWithPath("applicants[].imageUrl").description("신청자 이미지"),
                                fieldWithPath("applicants[].profileUrl").description("신청자 깃헙주소"),
                                fieldWithPath("applicants[].reasonForApplication").description("지원 동기"),
                                fieldWithPath("applicants[].applicationDate").description("신청자 참가일"),
                                fieldWithPath("tags").description("태그 정보"),
                                fieldWithPath("createdAt").description("스터디 등록일")
                        )
                ));
    }

    @Test
    @DisplayName("스터디 신청인이 스터디를 신청하면 스터디 신청자에 포함되어야 한다.")
    void apply() throws Exception {
        // given
        Member member = memberRepository.save(MemberFixture.create(true));
        String token = tokenProvider.createAccessToken(member.getId(), member.getRole());

        Member studyOwner = memberRepository.save(MemberFixture.create(true));
        Study study = StudyFixture.create(studyOwner);
        studyRepository.save(study);

        ApplyRequest applyRequest = ApplyRequestFixture.create();

        String json = objectMapper.writeValueAsString(applyRequest);

        // expected
        mockMvc.perform(patch("/api/study/{studyId}/apply", study.getId())
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .header(AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isCreated())
                .andDo(document("study-apply",
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("Bearer auth credentials")
                        ),
                        pathParameters(
                                parameterWithName("studyId").description("스터디 아이디 type(Long)")
                        ),
                        requestFields(
                                fieldWithPath("reasonForApplication").description("지원 동기/ 지원 내용")
                        )
                ));
    }

    @Test
    @DisplayName("스터디 신청인원은 스터디장이 승인할 수 있다.")
    void approve() throws Exception {
        // given
        Member studyOwner = memberRepository.save(MemberFixture.create(true));
        String token = tokenProvider.createAccessToken(studyOwner.getId(), studyOwner.getRole());
        Study study = StudyFixture.create(studyOwner);
        studyRepository.save(study);
        Member member = memberRepository.save(MemberFixture.createGithubMember());
        Applicant applicant = ApplicantFixture.create(study, member);
        study.addApplicant(applicant);

        // expected
        mockMvc.perform(patch("/api/study/{studyId}/approve/{applicantId}", study.getId(), member.getId())
                        .header(AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isCreated())
                .andDo(document("study-approve",
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("Bearer auth credentials")
                        ),
                        pathParameters(
                                parameterWithName("studyId").description("스터디 아이디 type(Long)"),
                                parameterWithName("applicantId").description("신청인 아이디")
                        )
                ));
    }

    @Test
    @DisplayName("스터디 여러건 조회 1페이지")
    void getStudies() throws Exception {
        // given
        Member studyOwner = memberRepository.save(MemberFixture.create(true));
        List<Study> studies = StudyFixture.createStudies(studyOwner);
        studyRepository.saveAll(studies);

        // expected
        mockMvc.perform(get("/api/study")
                        .param("page", "1")
                        .param("size", "8")
                )
                .andExpect(status().isOk())
                .andDo(document("study-get-studies",
                        requestParameters(
                                parameterWithName("page").description("페이지"),
                                parameterWithName("size").description("페이지 내 게시물 수")
                        ),
                        responseFields(
                                fieldWithPath("totalPage").description("총 페이지 수"),
                                fieldWithPath("studyResponses").description("스터디 게시물들"),
                                fieldWithPath("studyResponses[].studyId").description("스터디 아이디"),
                                fieldWithPath("studyResponses[].title").description("스터디 제목"),
                                fieldWithPath("studyResponses[].studyType").description("스터디 타입"),
                                fieldWithPath("studyResponses[].thumbnail").description("스터디 썸네일"),
                                fieldWithPath("studyResponses[].studyStatus").description("스터디 상태"),
                                fieldWithPath("studyResponses[].currentMemberCount").description("현재인원"),
                                fieldWithPath("studyResponses[].maxMemberCount").description("정원"),
                                fieldWithPath("studyResponses[].startDate").description("스터디 시작일"),
                                fieldWithPath("studyResponses[].endDate").description("스터디 종료일"),
                                fieldWithPath("studyResponses[].tags").description("스터디 태그들"),
                                fieldWithPath("studyResponses[].tags[]").description("스터디 태그 정보"),
                                fieldWithPath("studyResponses[].createdAt").description("스터디 등록일")
                        )
                ));
    }

    @Test
    @DisplayName("나의 신청 스터디 여러건 조회")
    void getMyApplies() throws Exception {
        // given
        Member applicantMember = memberRepository.save(MemberFixture.create(true));
        String token = tokenProvider.createAccessToken(applicantMember.getId(), applicantMember.getRole());

        Member studyOwner = memberRepository.save(MemberFixture.createGithubMember());
        List<Study> studies = IntStream.range(0, 10)
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
                                    .applicants(new HashSet<>())
                                    .maxMemberCount(30)
                                    .thumbnail("#00000")
                                    .tags(List.of(new Tag("태그1"), new Tag("태그2")))
                                    .build();
                    Applicant applicant = ApplicantFixture.create(study, applicantMember);
                    applicantRepository.save(applicant);
                    study.addApplicant(applicant);
                    return study;
                })
                .collect(Collectors.toList());
        studyRepository.saveAll(studies);

        // expected
        mockMvc.perform(get("/api/study/my/applies")
                        .header(AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andDo(document("study-get-myApplies",
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("Bearer auth credentials")
                        ),
                        responseFields(
                                fieldWithPath("totalPage").description("총 페이지 수"),
                                fieldWithPath("studyResponses").description("스터디 게시물들"),
                                fieldWithPath("studyResponses[].studyId").description("스터디 아이디"),
                                fieldWithPath("studyResponses[].title").description("스터디 제목"),
                                fieldWithPath("studyResponses[].studyType").description("스터디 타입"),
                                fieldWithPath("studyResponses[].thumbnail").description("스터디 썸네일"),
                                fieldWithPath("studyResponses[].studyStatus").description("스터디 상태"),
                                fieldWithPath("studyResponses[].currentMemberCount").description("현재인원"),
                                fieldWithPath("studyResponses[].maxMemberCount").description("정원"),
                                fieldWithPath("studyResponses[].startDate").description("스터디 시작일"),
                                fieldWithPath("studyResponses[].endDate").description("스터디 종료일"),
                                fieldWithPath("studyResponses[].tags").description("스터디 태그들"),
                                fieldWithPath("studyResponses[].tags[]").description("스터디 태그 정보"),
                                fieldWithPath("studyResponses[].createdAt").description("스터디 등록일")
                        )
                ));
    }

    @Test
    @DisplayName("나의 참가 스터디 조회")
    void getMyParticipates() throws Exception {
        // given
        Member applicantMember = memberRepository.save(MemberFixture.create(true));
        String token = tokenProvider.createAccessToken(applicantMember.getId(), applicantMember.getRole());

        Member studyOwner = memberRepository.save(MemberFixture.createGithubMember());
        List<Study> studies = IntStream.range(0, 10)
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
                                    .participants(new HashSet<>())
                                    .maxMemberCount(30)
                                    .thumbnail("#00000")
                                    .tags(List.of(new Tag("태그1"), new Tag("태그2")))
                                    .build();
                    Participant participant = new Participant(study, applicantMember, LocalDate.now().plusDays(i));
                    participantRepository.save(participant);
                    study.initParticipants(participant);
                    return study;
                })
                .collect(Collectors.toList());
        studyRepository.saveAll(studies);

        // expected
        mockMvc.perform(get("/api/study/my/participates")
                        .header(AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andDo(document("study-get-myParticipates",
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("Bearer auth credentials")
                        ),
                        responseFields(
                                fieldWithPath("totalPage").description("총 페이지 수"),
                                fieldWithPath("studyResponses").description("스터디 게시물들"),
                                fieldWithPath("studyResponses[].studyId").description("스터디 아이디"),
                                fieldWithPath("studyResponses[].title").description("스터디 제목"),
                                fieldWithPath("studyResponses[].studyType").description("스터디 타입"),
                                fieldWithPath("studyResponses[].thumbnail").description("스터디 썸네일"),
                                fieldWithPath("studyResponses[].studyStatus").description("스터디 상태"),
                                fieldWithPath("studyResponses[].currentMemberCount").description("현재인원"),
                                fieldWithPath("studyResponses[].maxMemberCount").description("정원"),
                                fieldWithPath("studyResponses[].startDate").description("스터디 시작일"),
                                fieldWithPath("studyResponses[].endDate").description("스터디 종료일"),
                                fieldWithPath("studyResponses[].tags").description("스터디 태그들"),
                                fieldWithPath("studyResponses[].tags[]").description("스터디 태그 정보"),
                                fieldWithPath("studyResponses[].createdAt").description("스터디 등록일")
                        )
                ));
    }

    @Test
    @DisplayName("스터디 수정하기")
    void edit() throws Exception {
        // given
        Member owner = memberRepository.save(MemberFixture.create(true));
        String token = tokenProvider.createAccessToken(owner.getId(), owner.getRole());

        Study study = studyRepository.save(StudyFixture.create(owner));

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

        // expected
        mockMvc.perform(put("/api/study/{studyId}", study.getId())
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andDo(document("study-edit",
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("Bearer auth credentials")
                        ),
                        pathParameters(
                                parameterWithName("studyId").description("스터디 아이디 type(Long)")
                        ),
                        requestFields(
                                fieldWithPath("title").description("제목"),
                                fieldWithPath("description").description("설명"),
                                fieldWithPath("studyType").description("스터디 타입 STUDY or MOGAKKO"),
                                fieldWithPath("thumbnail").description("썸네일 색상코드"),
                                fieldWithPath("startDate").description("스터디 시작일 포멧 (yy-MM-dd)"),
                                fieldWithPath("endDate").description("스터디 종료일 포멧 (yy-MM-dd)"),
                                fieldWithPath("maxMemberCount").description("스터디 최대인원"),
                                fieldWithPath("tags").description("태그 type(List)")
                        )
                ));
    }

    @Test
    @DisplayName("스터디 삭제하기")
    void delete() throws Exception {
        // given
        Member owner = memberRepository.save(MemberFixture.create(true));
        String token = tokenProvider.createAccessToken(owner.getId(), owner.getRole());
        Study study = studyRepository.save(StudyFixture.create(owner));

        // expected
        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/study/{studyId}", study.getId())
                        .header(AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isNoContent())
                .andDo(document("study-delete",
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("Bearer auth credentials")
                        ),
                        pathParameters(
                                parameterWithName("studyId").description("스터디 아이디 type(Long)")
                        )
                ));
    }

    @Test
    @DisplayName("스터디 탈퇴하기")
    void withdraw() throws Exception {
        // given
        Member owner = memberRepository.save(MemberFixture.createGithubMember());
        Study study = studyRepository.save(StudyFixture.create(owner));

        Member participantMember = memberRepository.save(MemberFixture.create(true));
        String token = tokenProvider.createAccessToken(participantMember.getId(), participantMember.getRole());

        Participant participant = new Participant(study, participantMember, LocalDate.now());
        participantRepository.save(participant);
        study.initParticipants(participant);

        // expected
        mockMvc.perform(patch("/api/study/{studyId}/withdraw", study.getId())
                        .header(AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isCreated())
                .andDo(document("study-withdraw",
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("Bearer auth credentials")
                        ),
                        pathParameters(
                                parameterWithName("studyId").description("스터디 아이디 type(Long)")
                        )
                ));
    }

    @Test
    @DisplayName("스터디 강퇴하기")
    void kick() throws Exception {
        // given
        Member owner = memberRepository.save(MemberFixture.create(true));
        String token = tokenProvider.createAccessToken(owner.getId(), owner.getRole());
        Study study = studyRepository.save(StudyFixture.create(owner));

        Member member = memberRepository.save(MemberFixture.createGithubMember());
        Participant participant = new Participant(study, member, LocalDate.now());
        participantRepository.save(participant);
        study.initParticipants(participant);

        // expected
        mockMvc.perform(patch("/api/study/{studyId}/kick/{memberId}", study.getId(), member.getId())
                        .header(AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isCreated())
                .andDo(document("study-kick",
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("Bearer auth credentials")
                        ),
                        pathParameters(
                                parameterWithName("studyId").description("스터디 아이디 type(Long)"),
                                parameterWithName("memberId").description("참가자 memberId")
                        )
                ));
    }

    @Test
    @DisplayName("스터디 신청 취소하기")
    void cancelApply() throws Exception {
        // given
        Member applicantMember = memberRepository.save(MemberFixture.create(true));
        String token = tokenProvider.createAccessToken(applicantMember.getId(), applicantMember.getRole());

        Member owner = memberRepository.save(MemberFixture.createGithubMember());
        Study study = studyRepository.save(StudyFixture.create(owner));
        Applicant applicant = ApplicantFixture.create(study, applicantMember);
        study.addApplicant(applicant);
        applicantRepository.save(applicant);

        // expected
        mockMvc.perform(patch("/api/study/{studyId}/apply-cancel", study.getId())
                        .header(AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isCreated())
                .andDo(document("study-apply-cancel",
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("Bearer auth credentials")
                        ),
                        pathParameters(
                                parameterWithName("studyId").description("스터디 아이디 type(Long)")
                        )
                ));

    }

    @Test
    @DisplayName("스터디 Search 여러건 조회 1페이지")
    void getSearchStudies() throws Exception {
        // given
        Member studyOwner = memberRepository.save(MemberFixture.create(true));
        List<Study> studies = StudyFixture.createStudies(studyOwner);
        studyRepository.saveAll(studies);

        // expected
        mockMvc.perform(get("/api/study")
                        .param("page", "1")
                        .param("size", "8")
                        .param("title", "입니")
                        .param("studyType", "STUDY")
                        .param("tag", "태그1")
                )
                .andExpect(status().isOk())
                .andDo(document("study-get-search-studies",
                        requestParameters(
                                parameterWithName("page").description("페이지"),
                                parameterWithName("size").description("페이지 내 게시물 수"),
                                parameterWithName("title").description("제목"),
                                parameterWithName("studyType").description("STUDY or MOGAKKO"),
                                parameterWithName("tag").description("태그")
                        ),
                        responseFields(
                                fieldWithPath("totalPage").description("총 페이지 수"),
                                fieldWithPath("studyResponses").description("스터디 게시물들"),
                                fieldWithPath("studyResponses[].studyId").description("스터디 아이디"),
                                fieldWithPath("studyResponses[].title").description("스터디 제목"),
                                fieldWithPath("studyResponses[].studyType").description("스터디 타입"),
                                fieldWithPath("studyResponses[].thumbnail").description("스터디 썸네일"),
                                fieldWithPath("studyResponses[].studyStatus").description("스터디 상태"),
                                fieldWithPath("studyResponses[].currentMemberCount").description("현재인원"),
                                fieldWithPath("studyResponses[].maxMemberCount").description("정원"),
                                fieldWithPath("studyResponses[].startDate").description("스터디 시작일"),
                                fieldWithPath("studyResponses[].endDate").description("스터디 종료일"),
                                fieldWithPath("studyResponses[].tags").description("스터디 태그들"),
                                fieldWithPath("studyResponses[].tags[]").description("스터디 태그 정보"),
                                fieldWithPath("studyResponses[].createdAt").description("스터디 등록일")
                        )
                ));
    }
}
