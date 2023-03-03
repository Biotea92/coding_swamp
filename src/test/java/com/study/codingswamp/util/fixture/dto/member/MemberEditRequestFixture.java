package com.study.codingswamp.util.fixture.dto.member;

import com.study.codingswamp.domain.member.dto.request.MemberEditRequest;
import org.springframework.mock.web.MockMultipartFile;

public class MemberEditRequestFixture {

    public static MemberEditRequest create() {
        return MemberEditRequest.builder()
                .username("kim")
                .profileUrl("http://profile")
                .imageFile(new MockMultipartFile("imageFile", "image".getBytes()))
                .build();
    }
}
