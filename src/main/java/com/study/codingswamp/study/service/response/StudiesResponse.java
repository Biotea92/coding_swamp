package com.study.codingswamp.study.service.response;

import lombok.Getter;

import java.util.List;

@Getter
public class StudiesResponse {

    private final List<StudyResponse> studyResponses;
    private final Integer totalPage;

    public StudiesResponse(List<StudyResponse> studyResponses, Integer totalPage) {
        this.studyResponses = studyResponses;
        this.totalPage = totalPage;
    }
}
