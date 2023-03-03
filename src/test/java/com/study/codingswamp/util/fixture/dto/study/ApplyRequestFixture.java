package com.study.codingswamp.util.fixture.dto.study;

import com.study.codingswamp.domain.study.dto.request.ApplyRequest;

public class ApplyRequestFixture {

    public static ApplyRequest create() {
        return new ApplyRequest("지원 동기입니다.");
    }
}
