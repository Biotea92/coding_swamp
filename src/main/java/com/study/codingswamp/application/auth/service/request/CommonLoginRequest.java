package com.study.codingswamp.application.auth.service.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommonLoginRequest {

    private String email;
    private String password;

    @Builder
    public CommonLoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
