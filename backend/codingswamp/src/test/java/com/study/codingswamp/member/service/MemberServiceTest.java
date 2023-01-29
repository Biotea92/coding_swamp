package com.study.codingswamp.member.service;

import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.Role;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import com.study.codingswamp.member.service.request.MemberSignupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void clear() {
        memberRepository.deleteAll();
    }

    @DisplayName("정상 회원가입")
    @Test
    void signup() {
        // given
        MemberSignupRequest memberSignupRequest = MemberSignupRequest.builder()
                .email("abc@gmail.com")
                .password("1q2w3e4r!")
                .username("hong")
                .build();

        // when
        memberService.signup(memberSignupRequest);

        // then
        Member member = memberRepository.findById(1L).orElseThrow();
        assertThat(member.getId()).isEqualTo(1L);
        assertThat(member.getEmail()).isEqualTo("abc@gmail.com");
        assertThat(passwordEncoder.matches("1q2w3e4r!", member.getPassword())).isTrue();
        assertThat(member.getUsername()).isEqualTo("hong");
        assertThat(member.getRole()).isEqualTo(Role.USER);
    }
}