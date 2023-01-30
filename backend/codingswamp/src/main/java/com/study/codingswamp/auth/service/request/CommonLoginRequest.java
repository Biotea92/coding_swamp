package com.study.codingswamp.auth.service.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CommonLoginRequest {

    private final String email;
    private final String password;

    @Builder
    public CommonLoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
