package com.study.codingswamp.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends CodingSwampException {

    private static final String MESSAGE = "NOT FOUND";

    public NotFoundException() {
        super(MESSAGE);
    }

    public NotFoundException(String fieldName, String message) {
        super(MESSAGE);
        addValidation(fieldName, message);
    }

    @Override
    public int getStatusCode() {
        return 404;
    }
}
