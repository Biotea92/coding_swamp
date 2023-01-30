package com.study.codingswamp.auth.service.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AccessTokenResponse {

    private String accessToken;
    private long expiredTime;
}
