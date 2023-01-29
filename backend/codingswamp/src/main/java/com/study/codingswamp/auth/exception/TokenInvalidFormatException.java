package com.study.codingswamp.auth.exception;

import com.study.codingswamp.common.exception.UnauthorizedException;

public class TokenInvalidFormatException extends UnauthorizedException {

    public TokenInvalidFormatException() {
        super("잘못된 토큰 형식 입니다.");
    }
}
