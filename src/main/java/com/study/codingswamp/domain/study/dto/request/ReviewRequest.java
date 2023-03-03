package com.study.codingswamp.domain.study.dto.request;

import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
public class ReviewRequest {

    @NotBlank
    private final String content;

    public ReviewRequest(String content) {
        this.content = content;
    }
}
