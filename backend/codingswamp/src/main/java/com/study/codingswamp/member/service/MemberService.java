package com.study.codingswamp.member.service;

import com.study.codingswamp.auth.service.request.CommonLoginRequest;
import com.study.codingswamp.common.file.FileStore;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import com.study.codingswamp.member.service.exception.MemberUnauthorizedException;
import com.study.codingswamp.member.service.request.MemberSignupRequest;
import com.study.codingswamp.member.service.response.MemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final FileStore fileStore;
    private final PasswordEncoder passwordEncoder;

    public MemberResponse signup(MemberSignupRequest memberSignupRequest) {
        String email = memberSignupRequest.getEmail();
        String password = memberSignupRequest.getPassword();
        String encodedPassword = passwordEncoder.encode(password);
        String username = memberSignupRequest.getUsername();
        String imageUrl = fileStore.storeFile(memberSignupRequest.getImageFile());

        Member member = new Member(email, encodedPassword, username, imageUrl);
        memberRepository.save(member);
        return new MemberResponse(member);
    }

    @Transactional
    public MemberResponse checkLogin(CommonLoginRequest request) {
        Member member = getMemberByEmail(request.getEmail());
        if (isMatchesPassword(request.getPassword(), member.getPassword())) {
            return new MemberResponse(member);
        }
        throw new MemberUnauthorizedException("wrong password");
    }

    private Member getMemberByEmail(String email) {
        return getByEmail(email)
                .orElseThrow(() -> new MemberUnauthorizedException("invalid Email"));
    }

    public Optional<Member> getByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    private boolean isMatchesPassword(String requestPassword, String password) {
        return passwordEncoder.matches(requestPassword, password);
    }
}
