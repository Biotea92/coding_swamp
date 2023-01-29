package com.study.codingswamp.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void clear() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("/api/member POST 요청시 회원가입이 완료된다.")
    void signupSuccess() throws Exception {
        // given
        String email = "seediu95@gmail.com";
        String password = "1q2w3e4r!";
        String username = "hong";

        // expected
        mockMvc.perform(post("/api/member?email=" + email + "&username=" + username + "&password=" + password)
                        .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.username").value("hong"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andDo(print());
    }


    @Test
    @DisplayName("/api/member POST 요청시 multipartFile을 제외한 모든 값은 필수다.")
    void signup_Can_Not_Be_Blank() throws Exception {
        mockMvc.perform(post("/api/member?")
                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validation.email").value("must not be blank"))
                .andExpect(jsonPath("$.validation.password").value("must not be blank"))
                .andExpect(jsonPath("$.validation.username").value("must not be blank"))
                .andDo(print());
    }

    @Test
    @DisplayName("/api/member POST 요청시 username은 3글자 이상이어야한다.")
    void signup_username_length_grater_than_3() throws Exception {
        // given
        String email = "seediu95@gmail.com";
        String password = "1q2w3e4r!";
        String username = "ho";

        // expected
        mockMvc.perform(post("/api/member?email=" + email + "&username=" + username + "&password=" + password)
                        .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validation.username").value("최소 3자 이상이어야 합니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("/api/member POST 요청시 password는 규칙을 따라야한다.")
    void signup_password_pattern_have_rule() throws Exception {
        // given
        String email = "seediu95@gmail.com";
        String password = "1q2w3e4r";
        String username = "hong";

        // expected
        mockMvc.perform(post("/api/member?email=" + email + "&username=" + username + "&password=" + password)
                        .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validation.password")
                        .value("최소 8자, 최소 하나의 문자, 하나의 숫자 및 하나의 특수 문자를 포함해야합니다."))
                .andDo(print());
    }
}