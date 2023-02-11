package com.study.codingswamp.study.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.codingswamp.auth.service.MemberPayload;
import com.study.codingswamp.auth.token.TokenProvider;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import com.study.codingswamp.study.domain.*;
import com.study.codingswamp.study.domain.repository.StudyRepository;
import com.study.codingswamp.study.service.request.ApplyRequest;
import com.study.codingswamp.study.service.request.StudyCreateRequest;
import com.study.codingswamp.utils.TestUtil;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
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
        StudyCreateRequest request = StudyCreateRequest.builder()
                .title("제목입니다.")
                .description("설명입니다.")
                .studyType("STUDY")
                .thumbnail("#000000")
                .startDate(LocalDate.of(2023, 2, 1))
                .endDate(LocalDate.of(2023, 2, 3))
                .maxMemberCount(30)
                .tags(List.of("태그1", "태그2"))
                .build();

        String token = new TestUtil().saveMemberAndGetToken(tokenProvider, memberRepository);
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
        new TestUtil().saveMemberAndGetToken(tokenProvider, memberRepository);
        Study study = getStudy(memberRepository.findById(1L).orElseThrow(RuntimeException::new));
        Member hong = memberRepository.save(new Member("member2@gmail.com", "1q2w3e4r!", "hong", "https://firebasestorage.googleapis.com/v0/b/coding-swamp.appspot.com/o/default_image%2Fcrocodile.png?alt=media"));
        study.addApplicant(new Applicant(hong, "지원 동기", LocalDate.now()));
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
        String token = new TestUtil().saveMemberAndGetToken(tokenProvider, memberRepository);
        Member studyOwner = createMember();
        Study study = getStudy(studyOwner);
        studyRepository.save(study);

        ApplyRequest applyRequest = new ApplyRequest("지원 동기입니다.");

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
        String token = new TestUtil().saveMemberAndGetToken(tokenProvider, memberRepository);
        Member studyOwner = memberRepository.findById(1L).orElseThrow(RuntimeException::new);
        Study study = getStudy(studyOwner);
        studyRepository.save(study);
        Member member = memberRepository.save(new Member("applicant@gmail.com", "testpassword", "kim", null));
        Applicant applicant = new Applicant(member, "지원동기", LocalDate.now());
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
        Member studyOwner = createMember();
        List<Study> studies = IntStream.range(0, 20)
                .mapToObj(i -> Study.builder()
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
                        .tags(List.of(new Tag("태그1"), new Tag("태그2")))
                        .build()
                )
                .collect(Collectors.toList());
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
        String token = new TestUtil().saveMemberAndGetToken(tokenProvider, memberRepository);
        MemberPayload payload = tokenProvider.getPayload("Bearer " + token);
        Member applicantMember = memberRepository.findById(payload.getId()).orElseThrow(RuntimeException::new);

        Member studyOwner = createMember();
        List<Study> studies = IntStream.range(0, 10)
                .mapToObj(i -> Study.builder()
                        .title("제목입니다. " + i)
                        .description("설명입니다. " + i)
                        .studyStatus(StudyStatus.PREPARING)
                        .studyType(StudyType.STUDY)
                        .startDate(LocalDate.now().plusDays(1))
                        .endDate(LocalDate.now().plusDays(2))
                        .owner(studyOwner)
                        .currentMemberCount(1)
                        .applicants(Set.of(new Applicant(applicantMember, "지원동기", LocalDate.now().plusDays(i))))
                        .maxMemberCount(30)
                        .thumbnail("#00000")
                        .tags(List.of(new Tag("태그1"), new Tag("태그2")))
                        .build()
                )
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

    Study getStudy(Member owner) {
        StudyCreateRequest request = StudyCreateRequest.builder()
                .title("제목입니다.")
                .description("설명입니다.")
                .studyType("STUDY")
                .thumbnail("#000000")
                .startDate(LocalDate.of(2023, 2, 1))
                .endDate(LocalDate.of(2023, 2, 3))
                .maxMemberCount(30)
                .tags(List.of("태그1", "태그2"))
                .build();

        return request.mapToStudy(owner);
    }

    private Member createMember() {
        Member member = new Member("abc@gmail.com", "1q2w3e4r!", "hong", null);
        memberRepository.save(member);
        return member;
    }
}
