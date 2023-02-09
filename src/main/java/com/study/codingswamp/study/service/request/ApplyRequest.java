package com.study.codingswamp.study.service.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApplyRequest {

    @NotBlank
    private String reasonForApplication;
}
