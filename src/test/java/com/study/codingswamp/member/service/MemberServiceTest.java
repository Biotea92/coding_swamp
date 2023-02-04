package com.study.codingswamp.member.service;

import com.study.codingswamp.auth.service.request.CommonLoginRequest;
import com.study.codingswamp.common.exception.ConflictException;
import com.study.codingswamp.common.exception.NotFoundException;
import com.study.codingswamp.common.exception.UnauthorizedException;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.Role;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import com.study.codingswamp.member.service.request.MemberSignupRequest;
import com.study.codingswamp.member.service.response.MemberResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clear() {
        jdbcTemplate.update("alter table member auto_increment= ?", 1);
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
        Member member = memberRepository.findById(1L).orElseThrow(RuntimeException::new);
        assertThat(member.getId()).isEqualTo(1L);
        assertThat(member.getEmail()).isEqualTo("abc@gmail.com");
        assertThat(passwordEncoder.matches("1q2w3e4r!", member.getPassword())).isTrue();
        assertThat(member.getUsername()).isEqualTo("hong");
        assertThat(member.getRole()).isEqualTo(Role.USER);
    }

    @DisplayName("로그인시 email, password는 확인되어야한다.")
    @Test
    void checkLogin() {
        // given
        saveMember();
        CommonLoginRequest request1 = CommonLoginRequest.builder()
                .email("abc@gmail.com")
                .password("1q2w3e4r!")
                .build();
        CommonLoginRequest request2 = CommonLoginRequest.builder()
                .email("notExistEmail")
                .password("1q2w3e4r!")
                .build();
        CommonLoginRequest request3 = CommonLoginRequest.builder()
                .email("abc@gmail.com")
                .password("wrongPassword")
                .build();

        // expected
        assertDoesNotThrow(() -> memberService.checkLogin(request1));
        assertThrows(
                UnauthorizedException.class,
                () -> memberService.checkLogin(request2)
        );
        assertThrows(
                UnauthorizedException.class,
                () -> memberService.checkLogin(request3)
        );
    }

    @DisplayName("이메일은 중복 체크 되어야한다.")
    @Test
    void duplicateEmail() {
        // given
        Member member = saveMember();

        // expected
        assertThrows(
                ConflictException.class,
                () -> memberService.duplicateEmailCheck(member.getEmail())
        );
        assertDoesNotThrow(
                () -> memberService.duplicateEmailCheck("efg@gmail.com")
        );
    }

    @DisplayName("사용자가 존재하지 않으면 예외를 발생시킨다.")
    @Test
    void checkExistMember() {
        assertThrows(
                NotFoundException.class,
                () -> memberService.checkExistMemberAndGet(1L)
        );
    }

    @DisplayName("사용자 단건조회")
    @Test
    void getMember() {
        // given
        Member member = saveMember();

        // when
        MemberResponse memberResponse = memberService.getMember(member.getId());

        // then
        assertThat(member.getId()).isEqualTo(memberResponse.getMemberId());
        assertThat(member.getEmail()).isEqualTo(memberResponse.getEmail());
        assertThat(member.getRole()).isEqualTo(memberResponse.getRole());
        assertThat(member.getUsername()).isEqualTo(memberResponse.getUsername());
        assertThat(member.getImageUrl()).isEqualTo(memberResponse.getImageUrl());
        assertThat(member.getProfileUrl()).isEqualTo(memberResponse.getProfileUrl());
        assertThat(member.getGithubId()).isEqualTo(memberResponse.getGithubId());
        assertThat(member.getJoinedAt()).isEqualTo(memberResponse.getJoinedAt());
    }

    private Member saveMember() {
        Member member = new Member("abc@gmail.com", passwordEncoder.encode("1q2w3e4r!"), "hong", null);
        return memberRepository.save(member);
    }
}