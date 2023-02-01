package com.study.codingswamp.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.codingswamp.auth.service.request.CommonLoginRequest;
import com.study.codingswamp.auth.token.TokenProvider;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.Role;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
    @DisplayName("로그인 요청시 로그인되어야한다.")
    void login() throws Exception {
        // given
        Member member = new Member("abc@gmail.com", passwordEncoder.encode("1q2w3e4r!"), "hong", null);
        memberRepository.save(member);

        CommonLoginRequest request = CommonLoginRequest.builder()
                .email("abc@gmail.com")
                .password("1q2w3e4r!")
                .build();

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/login/{loginType}", "common")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().isOk())
                .andDo(print());
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
                .andExpect(jsonPath("$.validation.email").value("데이터에 없는 이메일입니다."))
                .andDo(print());
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
                .andExpect(jsonPath("$.validation.password").value("잘못된 비밀번호입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("새로은 토큰을 refresh한다.")
    void refresh() throws Exception {
        Member member = new Member("abc@gmail.com", passwordEncoder.encode("1q2w3e4r!"), "hong", null);
        memberRepository.save(member);
        String token = tokenProvider.createAccessToken(1L, Role.USER);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiredTime").isNotEmpty());
    }

}