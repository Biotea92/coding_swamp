package com.study.codingswamp.auth.service;

import com.study.codingswamp.auth.service.request.CommonLoginRequest;
import com.study.codingswamp.auth.service.response.AccessTokenResponse;
import com.study.codingswamp.auth.token.TokenProvider;
import com.study.codingswamp.member.service.MemberService;
import com.study.codingswamp.member.service.response.MemberResponse;
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
        String token = tokenProvider.createAccessToken(memberResponse.getId(), memberResponse.getRole());
        return new AccessTokenResponse(token, tokenProvider.getValidityInMilliseconds());
    }
}
