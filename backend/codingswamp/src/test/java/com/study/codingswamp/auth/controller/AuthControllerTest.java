package com.study.codingswamp.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.codingswamp.auth.service.request.CommonLoginRequest;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clear() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("/api/auth/login/common POST 요청시 로그인되어야한다.")
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
    @DisplayName("/api/auth/login/common POST 요청시 이메일이 존재하지 않으면 에러메시지가 출력된다.")
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
    @DisplayName("/api/auth/login/common POST 요청시 비밀번호가 맞지않으면 에러메시지가 출력된다.")
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
}