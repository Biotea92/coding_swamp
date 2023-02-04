package com.study.codingswamp.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@Transactional
public class MemberControllerDocTest {

    @Autowired
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();

        jdbcTemplate.update("alter table member auto_increment= ?", 1);
    }

    @Test
    @DisplayName("회원가입 요청시 회원가입이 완료되어야 한다.")
    void signup() throws Exception {
        // given
        String email = "seediu95@gmail.com";
        String password = "1q2w3e4r!";
        String username = "hong";
        MockMultipartFile imageFile = new MockMultipartFile("imageFile", "image".getBytes());

        // expected
        mockMvc.perform(multipart("/api/member")
                        .file(imageFile)
                        .param("email", email)
                        .param("password", password)
                        .param("username", username)
                ).andExpect(status().isOk())
                .andDo(document("member-signup",
                        requestParts(
                                partWithName("imageFile").description("파일 업로드")
                        ),
                        requestParameters(
                                parameterWithName("email").description("이메일"),
                                parameterWithName("password").description("비밀번호"),
                                parameterWithName("username").description("사용자이름")
                        ),
                        responseFields(
                                fieldWithPath("memberId").description("회원 고유번호"),
                                fieldWithPath("email").description("회원 이메일"),
                                fieldWithPath("githubId").description("깃헙 고유아이디"),
                                fieldWithPath("username").description("username or github username"),
                                fieldWithPath("imageUrl").description("회원 이미지"),
                                fieldWithPath("profileUrl").description("깃헙 url"),
                                fieldWithPath("role").description("권한"),
                                fieldWithPath("joinedAt").description("가입일")
                        )
                ));
    }

    @Test
    @DisplayName("회원 단건조회가 완료되어야 한다.")
    void getMember() throws Exception {
        Member member = saveMember();

        mockMvc.perform(get("/api/member/{memberId}", member.getId()))
                .andExpect(status().isOk())
                .andDo(document("member-get",
                        pathParameters(
                                parameterWithName("memberId").description("회원고유번호")
                        ),
                        responseFields(
                                fieldWithPath("memberId").description("회원 고유번호"),
                                fieldWithPath("email").description("회원 이메일"),
                                fieldWithPath("githubId").description("깃헙 고유아이디"),
                                fieldWithPath("username").description("username or github username"),
                                fieldWithPath("imageUrl").description("회원 이미지"),
                                fieldWithPath("profileUrl").description("깃헙 url"),
                                fieldWithPath("role").description("권한"),
                                fieldWithPath("joinedAt").description("가입일")
                        )
                ));
    }

    private Member saveMember() {
        Member member = new Member("abc@gmail.com", "1q2w3e4r!", "hong", "https://firebasestorage.googleapis.com/v0/b/coding-swamp.appspot.com/o/default_image%2Fcrocodile.png?alt=media");
        return memberRepository.save(member);
    }
}
