package com.study.codingswamp.member.service;

import com.study.codingswamp.application.auth.service.request.CommonLoginRequest;
import com.study.codingswamp.domain.member.dto.request.MemberEditRequest;
import com.study.codingswamp.domain.member.dto.request.MemberSignupRequest;
import com.study.codingswamp.domain.member.dto.response.MemberResponse;
import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.member.entity.Role;
import com.study.codingswamp.domain.member.repository.MemberRepository;
import com.study.codingswamp.domain.member.service.MemberService;
import com.study.codingswamp.exception.ConflictException;
import com.study.codingswamp.exception.NotFoundException;
import com.study.codingswamp.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
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
        saveMemberAndGet();
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
        Member member = saveMemberAndGet();

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
        Member member = saveMemberAndGet();

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
        Member member = saveMemberAndGet();
        MemberEditRequest editRequest = MemberEditRequest.builder()
                .username("kim")
                .profileUrl("http://profile")
                .imageFile(new MockMultipartFile("imageFile", "image".getBytes()))
                .build();

        // when
        MemberResponse editResponse = memberService.edit(member.getId(), editRequest);

        assertThat(editResponse.getMemberId()).isEqualTo(member.getId());
        assertThat(editResponse.getUsername()).isEqualTo("kim");
        assertThat(editResponse.getProfileUrl()).isEqualTo("http://profile");
        assertThat(editResponse.getImageUrl()).isNotEqualTo("null");
    }

    @DisplayName("깃허브 사용자는 정보수정이 불가능 하다.")
    @Test
    void editGithubMember() {
        // given
        Member member = saveGithubMemberAndGet();
        MemberEditRequest editRequest = MemberEditRequest.builder()
                .username("kim")
                .profileUrl("http://profile")
                .imageFile(new MockMultipartFile("imageFile", "image".getBytes()))
                .build();

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
        Member member = saveMemberAndGet();
        System.out.println(member.getImageUrl());

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
        Member member = saveGithubMemberAndGet();

        // expected
        assertThrows(
                UnauthorizedException.class,
                () -> memberService.delete(member.getId())
        );
    }

    private Member saveMemberAndGet() {
        Member member = new Member("abc@gmail.com", passwordEncoder.encode("1q2w3e4r!"), "hong", null);
        return memberRepository.save(member);
    }

    private Member saveGithubMemberAndGet() {
        Member member = new Member("seediu95@gmail.com", 102938L, "seediu", "https//image", "https//profile");
        memberRepository.save(member);
        return member;
    }
}