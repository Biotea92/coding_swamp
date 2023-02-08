package com.study.codingswamp.common.exception;

public class ConflictException extends CodingSwampException{

    private static final String MESSAGE = "리소스의 충돌이 일어났습니다.";

    public ConflictException() {
        super(MESSAGE);
    }

    public ConflictException(String fieldName, String message) {
        super(MESSAGE);
        addValidation(fieldName, message);
    }

    @Override
    public int getStatusCode() {
        return 409;
    }
}
