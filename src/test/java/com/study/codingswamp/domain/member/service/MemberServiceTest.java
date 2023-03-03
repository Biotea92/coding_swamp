package com.study.codingswamp.domain.member.service;

import com.study.codingswamp.application.auth.service.request.CommonLoginRequest;
import com.study.codingswamp.domain.member.dto.request.MemberEditRequest;
import com.study.codingswamp.domain.member.dto.request.MemberSignupRequest;
import com.study.codingswamp.domain.member.dto.response.MemberResponse;
import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.member.entity.Role;
import com.study.codingswamp.domain.member.repository.MemberRepository;
import com.study.codingswamp.exception.ConflictException;
import com.study.codingswamp.exception.NotFoundException;
import com.study.codingswamp.exception.UnauthorizedException;
import com.study.codingswamp.util.fixture.dto.member.CommonLoginRequestFixture;
import com.study.codingswamp.util.fixture.dto.member.MemberEditRequestFixture;
import com.study.codingswamp.util.fixture.dto.member.MemberSignupRequestFixture;
import com.study.codingswamp.util.fixture.entity.member.MemberFixture;
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
        MemberSignupRequest memberSignupRequest = MemberSignupRequestFixture.create();

        // when
        memberService.signup(memberSignupRequest);

        // then
        Member member = memberRepository.findById(1L).orElseThrow(RuntimeException::new);
        assertThat(member.getId()).isEqualTo(1L);
        assertThat(member.getEmail()).isEqualTo(memberSignupRequest.getEmail());
        assertThat(passwordEncoder.matches(memberSignupRequest.getPassword(), member.getPassword())).isTrue();
        assertThat(member.getUsername()).isEqualTo(memberSignupRequest.getUsername());
        assertThat(member.getRole()).isEqualTo(Role.USER);
    }

    @DisplayName("로그인시 email, password는 확인되어야한다.")
    @Test
    void checkLogin() {
        // given
        Member member = MemberFixture.create("abc@gmail.com", passwordEncoder.encode("1q2w3e4r!"), "hong");
        memberRepository.save(member);
        CommonLoginRequest request1 = CommonLoginRequestFixture.create("abc@gmail.com", "1q2w3e4r!");
        CommonLoginRequest request2 = CommonLoginRequestFixture.create("notExistEmail", "1q2w3e4r!");
        CommonLoginRequest request3 = CommonLoginRequestFixture.create("abc@gmail.com", "wrongPassword");

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
        Member member = MemberFixture.create("abc@gmail.com", passwordEncoder.encode("1q2w3e4r!"), "hong");
        memberRepository.save(member);

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
        Member member = MemberFixture.create("abc@gmail.com", passwordEncoder.encode("1q2w3e4r!"), "hong");
        memberRepository.save(member);

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

    @DisplayName("사용자 정보 수정")
    @Test
    void edit() {
        // given
        Member member = MemberFixture.create("abc@gmail.com", passwordEncoder.encode("1q2w3e4r!"), "hong");
        memberRepository.save(member);
        MemberEditRequest editRequest = MemberEditRequestFixture.create();

        // when
        MemberResponse editResponse = memberService.edit(member.getId(), editRequest);

        // expected
        assertThat(editResponse.getMemberId()).isEqualTo(member.getId());
        assertThat(editResponse.getUsername()).isEqualTo(editRequest.getUsername());
        assertThat(editResponse.getProfileUrl()).isEqualTo(editRequest.getProfileUrl());
        assertThat(editResponse.getImageUrl()).isNotNull();
    }

    @DisplayName("깃허브 사용자는 정보수정이 불가능 하다.")
    @Test
    void editGithubMember() {
        // given
        Member member = MemberFixture.createGithubMember();
        memberRepository.save(member);

        MemberEditRequest editRequest = MemberEditRequestFixture.create();

        // expected
        assertThrows(
                UnauthorizedException.class,
                () -> memberService.edit(member.getId(), editRequest)
        );
    }

    @DisplayName("사용자 탈퇴")
    @Test
    void delete() {
        // given
        Member member = MemberFixture.create("abc@gmail.com", passwordEncoder.encode("1q2w3e4r!"), "hong");
        memberRepository.save(member);

        // when
        memberService.delete(member.getId());

        // then
        assertThrows(
               RuntimeException.class,
                () -> memberRepository.findById(member.getId()).orElseThrow(RuntimeException::new)
        );
    }

    @DisplayName("깃허브 사용자는 탈퇴가 불가능 하다.")
    @Test
    void deleteGithubMember() {
        // given
        Member member = MemberFixture.createGithubMember();
        memberRepository.save(member);

        // expected
        assertThrows(
                UnauthorizedException.class,
                () -> memberService.delete(member.getId())
        );
    }
}