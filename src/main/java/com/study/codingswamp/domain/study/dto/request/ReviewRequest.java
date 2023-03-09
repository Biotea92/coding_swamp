package com.study.codingswamp.domain.study.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class ReviewRequest {

    @NotBlank
    private String content;

    public ReviewRequest(String content) {
        this.content = content;
    }
}
