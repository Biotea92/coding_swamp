package com.study.codingswamp.study.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.codingswamp.auth.token.TokenProvider;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import com.study.codingswamp.study.domain.Applicant;
import com.study.codingswamp.study.domain.Study;
import com.study.codingswamp.study.domain.repository.StudyRepository;
import com.study.codingswamp.study.service.request.StudyCreateRequest;
import com.study.codingswamp.utils.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
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
                .apply(documentationConfiguration(restDocumentation))
                .build();
        jdbcTemplate.update("alter table study auto_increment= ?", 1);
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

        String token = new TestUtil().saveMemberAndGetToken(tokenProvider, memberRepository, jdbcTemplate);
        String json = objectMapper.writeValueAsString(request);
        // expected

        mockMvc.perform(post("/api/study")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isCreated())
                .andDo(document("study-create",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer auth credentials")
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
        new TestUtil().saveMemberAndGetToken(tokenProvider, memberRepository, jdbcTemplate);
        Study study = saveStudy(1L);
        memberRepository.save(new Member("member2@gmail.com", "1q2w3e4r!", "hong", "https://firebasestorage.googleapis.com/v0/b/coding-swamp.appspot.com/o/default_image%2Fcrocodile.png?alt=media"));
        study.getApplicants().add(new Applicant(2L, LocalDate.now()));
        Study saveStudy = studyRepository.save(study);

        // expected
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/study/{studyId}", saveStudy.getId()))
                .andExpect(status().isOk())
                .andDo(document("study-get-detail",
                        pathParameters(
                               parameterWithName("studyId").description("스터디 아이디 type(Long)")
                        ),
                        responseFields(
                                fieldWithPath("studyId").type(JsonFieldType.NUMBER).description("스터디 아이디"),
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
                                fieldWithPath("applicants[].applicationDate").description("신청자 참가일"),
                                fieldWithPath("tags").description("태그 정보"),
                                fieldWithPath("createdAt").description("스터디 등록일")
                        )
                ));
    }

    Study saveStudy(Long ownerId) {
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

        return request.mapToStudy(ownerId);
    }
}
