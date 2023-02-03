package com.study.codingswamp.common.exception;

public class ForbiddenException extends CodingSwampException{

    private static final String MESSAGE = "권한이 없습니다.";

    public ForbiddenException() {
        super(MESSAGE);
    }

    public ForbiddenException(String fieldName, String message) {
        super(MESSAGE);
        addValidation(fieldName, message);
    }

    @Override
    public int getStatusCode() {
        return 403;
    }
}
