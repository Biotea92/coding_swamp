package com.study.codingswamp.auth.exception;

import com.study.codingswamp.common.exception.UnauthorizedException;

public class TokenExpirationException extends UnauthorizedException {

    public TokenExpirationException() {
        super("만료된 토큰입니다.");
    }
}
