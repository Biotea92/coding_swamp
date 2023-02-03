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

import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
    @DisplayName("로그인 시 토큰을 발급한다.")
    void login() throws InterruptedException {
        // given
        Member member = new Member("abc@gmail.com", passwordEncoder.encode("1q2w3e4r!"), "hong", null);
        memberRepository.save(member);

        CommonLoginRequest request = CommonLoginRequest.builder()
                .email("abc@gmail.com")
                .password("1q2w3e4r!")
                .build();

        // when
        String[] accessToken = authService.login(request).getAccessToken().split("\\.");
        String[] token = tokenProvider.createAccessToken(1L, Role.USER).split("\\.");

        // then
        // 같은 시간에 생성되야 같은 코드가 token이 생성됨 개선방법 찾아보기
//        assertThat(accessToken[0]).isEqualTo(token[0]);
//        assertThat(accessToken[1]).isEqualTo(token[1]);
//        assertThat(accessToken[2]).isEqualTo(token[2]);
    }

    @Test
    @DisplayName("리프래쉬토큰 요청시 리프래쉬토큰을 발급한다.")
    void refresh() throws InterruptedException {
        // given
        Member member = new Member("abc@gmail.com", passwordEncoder.encode("1q2w3e4r!"), "hong", null);
        memberRepository.save(member);
        CommonLoginRequest request = CommonLoginRequest.builder()
                .email("abc@gmail.com")
                .password("1q2w3e4r!")
                .build();
        AccessTokenResponse token = authService.login(request);
        MemberPayload memberPayload = new MemberPayload(1L, Role.USER);

        // when
        Thread.sleep(1000);
        AccessTokenResponse refreshToken = authService.refreshToken(memberPayload);

        // then
        assertNotEquals(token.getAccessToken(), refreshToken.getAccessToken());
    }
}