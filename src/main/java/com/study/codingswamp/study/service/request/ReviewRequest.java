package com.study.codingswamp.study.service.request;

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
