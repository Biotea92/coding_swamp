package com.study.codingswamp.util.fixture.dto.member;

import com.study.codingswamp.application.auth.service.request.CommonLoginRequest;

public class CommonLoginRequestFixture {

    public static CommonLoginRequest create(String email, String password) {
        return CommonLoginRequest.builder()
                .email(email)
                .password(password)
                .build();
    }
}
