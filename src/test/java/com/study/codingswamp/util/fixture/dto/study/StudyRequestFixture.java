package com.study.codingswamp.util.fixture.dto.study;

import com.study.codingswamp.domain.study.dto.request.StudyRequest;

import java.time.LocalDate;
import java.util.List;

public class StudyRequestFixture {

    public static StudyRequest create() {
        return StudyRequest.builder()
                .title("제목입니다.")
                .description("설명입니다.")
                .studyType("STUDY")
                .thumbnail("#000000")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .maxMemberCount(30)
                .tags(List.of("태그1", "태그2"))
                .build();
    }

    public static StudyRequest create(int maxMemberCount) {
        return StudyRequest.builder()
                .title("제목입니다.")
                .description("설명입니다.")
                .studyType("STUDY")
                .thumbnail("#000000")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .maxMemberCount(maxMemberCount)
                .tags(List.of("태그1", "태그2"))
                .build();
    }
}
