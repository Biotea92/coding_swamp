package com.study.codingswamp.common.exception;

import lombok.Getter;

@Getter
public class UnauthorizedException extends CodingSwampException {

    private static final String MESSAGE = "인증되지 않은 사용자입니다.";

    public UnauthorizedException() {
        super(MESSAGE);
    }

    public UnauthorizedException(String fieldName, String message) {
        super(MESSAGE);
        addValidation(fieldName, message);
    }

    @Override
    public int getStatusCode() {
        return 401;
    }
}
