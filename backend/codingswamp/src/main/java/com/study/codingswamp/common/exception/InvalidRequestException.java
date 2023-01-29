package com.study.codingswamp.common.exception;

import lombok.Getter;

@Getter
public class InvalidRequestException extends CodingSwampException{

    private static final String MESSAGE = "잘못된 요청입니다.";

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String fieldName, String message) {
        super(message);
        addValidation(fieldName, message);
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
