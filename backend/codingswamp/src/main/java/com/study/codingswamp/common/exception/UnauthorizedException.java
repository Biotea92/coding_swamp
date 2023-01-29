package com.study.codingswamp.common.exception;

import lombok.Getter;

@Getter
public class UnauthorizedException extends CodingSwampException {

    public UnauthorizedException(String message) {
        super(message);
    }

    @Override
    public int getStatusCode() {
        return 401;
    }
}
