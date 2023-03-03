package com.study.codingswamp.util.fixture.dto.member;

import com.study.codingswamp.domain.member.dto.request.MemberSignupRequest;

public class MemberSignupRequestFixture {

    public static MemberSignupRequest create() {
        return MemberSignupRequest.builder()
                .email("abc@gmail.com")
                .password("1q2w3e4r!")
                .username("hong")
                .build();
    }
}
