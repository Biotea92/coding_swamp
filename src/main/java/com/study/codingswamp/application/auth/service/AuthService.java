package com.study.codingswamp.application.auth.service;

import com.study.codingswamp.application.auth.MemberPayload;
import com.study.codingswamp.application.auth.oauth.response.GithubProfileResponse;
import com.study.codingswamp.application.auth.service.request.CommonLoginRequest;
import com.study.codingswamp.application.auth.token.TokenProvider;
import com.study.codingswamp.application.auth.service.response.AccessTokenResponse;
import com.study.codingswamp.domain.member.service.MemberService;
import com.study.codingswamp.domain.member.dto.response.MemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;
    private final MemberService memberService;

    @Transactional
    public AccessTokenResponse login(CommonLoginRequest request) {
        MemberResponse memberResponse = memberService.checkLogin(request);
        return getAccessTokenResponse(memberResponse);
    }

    public AccessTokenResponse refreshToken(MemberPayload memberPayload) {
        memberService.checkExistMemberAndGet(memberPayload.getId());

        String token = tokenProvider.createAccessToken(memberPayload.getId(), memberPayload.getRole());
        return new AccessTokenResponse(token, tokenProvider.getValidityInMilliseconds());
    }

    public AccessTokenResponse githubLogin(GithubProfileResponse profileResponse) {
        MemberResponse memberResponse = memberService.saveOrUpdate(profileResponse.toMember());
        return getAccessTokenResponse(memberResponse);
    }

    private AccessTokenResponse getAccessTokenResponse(MemberResponse memberResponse) {
        String token = tokenProvider.createAccessToken(memberResponse.getMemberId(), memberResponse.getRole());
        return new AccessTokenResponse(token, tokenProvider.getValidityInMilliseconds());
    }
}
