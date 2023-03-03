package com.study.codingswamp.presentation.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.codingswamp.application.auth.oauth.GithubOauthClient;
import com.study.codingswamp.application.auth.oauth.response.GithubProfileResponse;
import com.study.codingswamp.application.auth.service.request.CommonLoginRequest;
import com.study.codingswamp.application.auth.service.request.MailAuthenticationRequest;
import com.study.codingswamp.application.auth.token.TokenProvider;
import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.member.repository.MemberRepository;
import com.study.codingswamp.util.fixture.entity.member.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.removeHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@Transactional
public class AuthControllerDocTest {
    @Autowired
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TokenProvider tokenProvider;

    @MockBean
    private GithubOauthClient githubOauthClient;

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint(), removeHeaders("Vary"))
                )
                .build();

        jdbcTemplate.update("alter table member auto_increment= ?", 1);
    }

    @Test
    @DisplayName("로그인 요청시 로그인되어야한다.")
    void login() throws Exception {
        Member member = new Member("abc@gmail.com", passwordEncoder.encode("1q2w3e4r!"), "hong", null);
        memberRepository.save(member);

        CommonLoginRequest request = CommonLoginRequest.builder()
                .email("abc@gmail.com")
                .password("1q2w3e4r!")
                .build();

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/login/common")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("member-login",
                        requestFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("password").description("비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("accessToken").description("jwt token"),
                                fieldWithPath("expiredTime").description("만료 밀리세컨드")
                        )
                ));
    }

    @Test
    @DisplayName("이메일 인증 이메일 발송 및 인증번호 생성")
    void emailAuth() throws Exception {
        MailAuthenticationRequest request = new MailAuthenticationRequest("seediu95@gmail.com");

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/email")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().isCreated())
                .andDo(document("auth-email",
                        requestFields(
                                fieldWithPath("email").description("메일발송 인증용 이메일")
                        )
                ));
    }

    @Test
    @DisplayName("이메일 인증번호를 확인한다.")
    void emailAuthCodeConfirm() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("authCode", "123456");
        session.setMaxInactiveInterval(180);

        mockMvc.perform(post("/api/auth/email/confirm")
                        .param("authCode", "123456")
                        .session(session)
                )
                .andExpect(status().isOk())
                .andDo(document("auth-email-confirm",
                        requestParameters(
                                parameterWithName("authCode").description("메일 인증코드")
                        )
                ));
    }

    @Test
    @DisplayName("토큰을 refresh한다.")
    void refresh() throws Exception {
        Member member = memberRepository.save(MemberFixture.create(true));
        String token = tokenProvider.createAccessToken(member.getId(), member.getRole());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andDo(document("auth-token-refresh",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer auth credentials")
                        ),
                        responseFields(
                                fieldWithPath("accessToken").description("jwt token"),
                                fieldWithPath("expiredTime").description("만료 밀리세컨드")
                        )
                ));
    }

    @Test
    @DisplayName("깃허브 로그인 요청")
    void githubLogin() throws Exception {
        GithubProfileResponse swamp = GithubProfileResponse.builder()
                .githubId("1")
                .imageUrl("http://imageUrl")
                .username("swamp")
                .profileUrl("http://profileUrl")
                .email("swamp@gmail.com")
                .build();

        given(githubOauthClient.getProfile("AuthorizationCode"))
                .willReturn(swamp);

        mockMvc.perform(post("/api/auth/login/github")
                        .param("code", "AuthorizationCode")
                )
                .andExpect(status().isOk())
                .andDo(document("auth-login-github",
                        requestParameters(
                                parameterWithName("code").description("Authorization code")
                        ),
                        responseFields(
                                fieldWithPath("accessToken").description("jwt token"),
                                fieldWithPath("expiredTime").description("만료 밀리세컨드")
                        )
                ));
    }
}
