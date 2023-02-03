package com.study.codingswamp.study.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.codingswamp.auth.token.TokenProvider;
import com.study.codingswamp.member.domain.repository.MemberRepository;
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
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
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
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
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

        TestUtil testUtil = new TestUtil();
        String token = testUtil.saveMemberAndGetToken(tokenProvider, memberRepository, jdbcTemplate);
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
}
