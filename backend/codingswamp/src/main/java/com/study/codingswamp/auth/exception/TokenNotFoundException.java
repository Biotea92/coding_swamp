package com.study.codingswamp.auth.exception;

import com.study.codingswamp.common.exception.UnauthorizedException;

public class TokenNotFoundException extends UnauthorizedException {

    public TokenNotFoundException() {
        super("토큰이 존재하지 않습니다.");
    }
}
