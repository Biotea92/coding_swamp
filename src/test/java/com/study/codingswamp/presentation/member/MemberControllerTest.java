package com.study.codingswamp.presentation.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clear() {
        jdbcTemplate.update("alter table member auto_increment= ?", 1);
    }

    @Test
    @DisplayName("회원가입 요청시 multipartFile을 제외한 모든 값은 필수다.")
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
    @DisplayName("회원가입 요청시 username은 3글자 이상이어야한다.")
    void signup_username_length_grater_than_3() throws Exception {
        // given
        String email = "seediu95@gmail.com";
        String password = "1q2w3e4r!";
        String username = "ho";
        MockMultipartFile multipartFile = new MockMultipartFile("imageFile", "image".getBytes());

        // expected
        mockMvc.perform(multipart("/api/member")
                        .file(multipartFile)
                        .param("email", email)
                        .param("username", username)
                        .param("password", password)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validation.username").value("최소 3자 이상 최대 20자 이하 이어야 합니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 요청시 password는 규칙을 따라야한다.")
    void signup_password_pattern_have_rule() throws Exception {
        // given
        String email = "seediu95@gmail.com";
        String password = "1q2w3e4r";
        String username = "hong";
        MockMultipartFile multipartFile = new MockMultipartFile("imageFile", "image".getBytes());

        // expected
        mockMvc.perform(multipart("/api/member")
                        .file(multipartFile)
                        .param("email", email)
                        .param("username", username)
                        .param("password", password)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validation.password")
                        .value("최소 8자, 최소 하나의 문자, 하나의 숫자 및 하나의 특수 문자를 포함해야합니다."))
                .andDo(print());
    }
}