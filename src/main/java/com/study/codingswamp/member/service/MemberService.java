package com.study.codingswamp.member.service;

import com.study.codingswamp.auth.service.MemberPayload;
import com.study.codingswamp.auth.service.request.CommonLoginRequest;
import com.study.codingswamp.common.exception.ConflictException;
import com.study.codingswamp.common.exception.NotFoundException;
import com.study.codingswamp.common.exception.UnauthorizedException;
import com.study.codingswamp.common.file.FileStore;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import com.study.codingswamp.member.service.request.MemberEditRequest;
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
        duplicateEmailCheck(email);
        String password = memberSignupRequest.getPassword();
        String encodedPassword = passwordEncoder.encode(password);
        String username = memberSignupRequest.getUsername();
        String imageUrl = fileStore.storeFile(memberSignupRequest.getImageFile());

        Member member = Member.builder()
                .email(email)
                .password(encodedPassword)
                .username(username)
                .imageUrl(imageUrl)
                .build();

        return new MemberResponse(memberRepository.save(member));
    }

    @Transactional
    public MemberResponse checkLogin(CommonLoginRequest request) {
        Member member = getMemberByEmail(request.getEmail());
        if (isMatchesPassword(request.getPassword(), member.getPassword())) {
            return new MemberResponse(member);
        }
        throw new UnauthorizedException("password", "잘못된 비밀번호입니다.");
    }

    public MemberResponse getMember(Long memberId) {
        Member member = checkExistMemberAndGet(memberId);
        return new MemberResponse(member);
    }

    @Transactional
    public MemberResponse saveOrUpdate(Member loginMember) {
        Optional<Member> findMember = memberRepository.findByGithubId(loginMember.getGithubId());
        if (findMember.isPresent()) {
            findMember.get().update(
                    loginMember.getUsername(), loginMember.getEmail(), loginMember.getImageUrl(), loginMember.getProfileUrl()
            );
            return new MemberResponse(findMember.get());
        }
        return new MemberResponse(memberRepository.save(loginMember));
    }

    public MemberResponse edit(MemberPayload memberPayload, MemberEditRequest request) {
        Long memberId = memberPayload.getId();
        Member member = checkExistMemberAndGet(memberId);
        if (member.getGithubId() == null) {
            throw new UnauthorizedException("github", "깃허브 사용자는 수정이 불가능합니다.");
        }

        fileUpdateOrSave(request, member);

        member.updateUsername(request.getUsername());

        if (request.getProfileUrl() != null) {
            member.updateProfileUrl(request.getProfileUrl());
        }
        return new MemberResponse(member);
    }

    private void fileUpdateOrSave(MemberEditRequest request, Member member) {
        if (request.getImageFile() != null) {
            if (!member.getImageUrl().equals("null")) {
                fileStore.deleteFile(member.getImageUrl());
            }
            String imageUrl = fileStore.storeFile(request.getImageFile());
            member.updateImageUrl(imageUrl);
        }
    }

    public void duplicateEmailCheck(String email) {
        if (getByEmail(email).isPresent()) {
            throw new ConflictException("email", "이메일이 중복입니다.");
        }
    }

    public Member checkExistMemberAndGet(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("member", "사용자를 찾을 수 없습니다."));
    }

    private Member getMemberByEmail(String email) {
        return getByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("email", "데이터에 없는 이메일입니다."));
    }

    private Optional<Member> getByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    private boolean isMatchesPassword(String requestPassword, String password) {
        return passwordEncoder.matches(requestPassword, password);
    }
}
