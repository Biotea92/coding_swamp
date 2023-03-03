package com.study.codingswamp.presentation.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.codingswamp.application.auth.service.request.CommonLoginRequest;
import com.study.codingswamp.application.auth.service.request.MailAuthenticationRequest;
import com.study.codingswamp.application.auth.token.TokenProvider;
import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.member.repository.MemberRepository;
import com.study.codingswamp.util.fixture.entity.member.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TokenProvider tokenProvider;

    @BeforeEach
    void clear() {
        jdbcTemplate.update("alter table member auto_increment= ?", 1);
    }

    @Test
    @DisplayName("로그인 요청시 이메일이 존재하지 않으면 에러메시지가 출력된다.")
    void loginErrorEmail() throws Exception {
        // given
        Member member = new Member("abc@gmail.com", passwordEncoder.encode("1q2w3e4r!"), "hong", null);
        memberRepository.save(member);

        CommonLoginRequest request = CommonLoginRequest.builder()
                .email("notExistEmail@gmail.com")
                .password("1q2w3e4r!")
                .build();

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/login/{loginType}", "common")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"))
                .andExpect(jsonPath("$.message").value("인증되지 않은 사용자입니다."))
                .andExpect(jsonPath("$.validation.email").value("데이터에 없는 이메일입니다."));
    }

    @Test
    @DisplayName("로그인 요청시 비밀번호가 맞지않으면 에러메시지가 출력된다.")
    void loginErrorPassword() throws Exception {
        // given
        Member member = new Member("abc@gmail.com", passwordEncoder.encode("1q2w3e4r!"), "hong", null);
        memberRepository.save(member);

        CommonLoginRequest request = CommonLoginRequest.builder()
                .email("abc@gmail.com")
                .password("1q2w3e4r!@")
                .build();

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/login/{loginType}", "common")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"))
                .andExpect(jsonPath("$.message").value("인증되지 않은 사용자입니다."))
                .andExpect(jsonPath("$.validation.password").value("잘못된 비밀번호입니다."));
    }

    @Test
    @DisplayName("새로은 토큰을 refresh한다.")
    void refresh() throws Exception {
        Member member = memberRepository.save(MemberFixture.create(true));
        String token = tokenProvider.createAccessToken(member.getId(), member.getRole());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiredTime").isNotEmpty());
    }

    @Test
    @DisplayName("이메일 인증번호 발송시 이메일 형식이 맞지않으면 에러메세지가 출력된다.")
    void emailAuthSend() throws Exception {
        MailAuthenticationRequest request = new MailAuthenticationRequest("notEmailFormat");

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/email")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validation.email").value("must be a well-formed email address"));
    }

    @Test
    @DisplayName("이메일 인증번호를 확인시 인증번호가 다르면 에러메세지가 출력된다.")
    void emailAuthCodeDoesNotMatch() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("authCode", "098765");
        session.setMaxInactiveInterval(180);

        mockMvc.perform(post("/api/auth/email/confirm")
                        .param("authCode", "123456")
                        .session(session)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"))
                .andExpect(jsonPath("$.message").value("인증되지 않은 사용자입니다."))
                .andExpect(jsonPath("$.validation.authCode").value("인증번호가 일치하지않습니다."));
    }

    @Test
    @DisplayName("이메일 인증번호를 확인시 세션이 없으면 에러메세지가 출력된다.")
    void emailAuthCodeNoSession() throws Exception {
        mockMvc.perform(post("/api/auth/email/confirm")
                        .param("authCode", "123456")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"))
                .andExpect(jsonPath("$.message").value("인증되지 않은 사용자입니다."))
                .andExpect(jsonPath("$.validation.authCode").value("세션이 만료되었습니다."));
    }
}