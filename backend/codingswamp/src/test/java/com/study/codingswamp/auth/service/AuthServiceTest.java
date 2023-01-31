package com.study.codingswamp.auth.service;

import com.study.codingswamp.auth.service.request.CommonLoginRequest;
import com.study.codingswamp.auth.service.response.AccessTokenResponse;
import com.study.codingswamp.auth.token.TokenProvider;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.Role;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private AuthService authService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clear() {
        jdbcTemplate.update("alter table member auto_increment= ?", 1);
    }
    @Test
    @DisplayName("요청시 로그인 되어야한다.")
    void login() {
        // given
        Member member = new Member("abc@gmail.com", passwordEncoder.encode("1q2w3e4r!"), "hong", null);
        memberRepository.save(member);

        CommonLoginRequest request = CommonLoginRequest.builder()
                .email("abc@gmail.com")
                .password("1q2w3e4r!")
                .build();

        // when
        AccessTokenResponse accessTokenResponse = authService.login(request);
        String token = tokenProvider.createAccessToken(1L, Role.USER);

        // then
//        assertThat(accessTokenResponse.getAccessToken()).isEqualTo(token);
    }
}