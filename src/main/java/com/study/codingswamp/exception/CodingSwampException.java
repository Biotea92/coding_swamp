package com.study.codingswamp.exception;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public abstract class CodingSwampException extends RuntimeException{

    public final Map<String, String> validation = new ConcurrentHashMap<>();

    public CodingSwampException(String message) {
        super(message);
    }

    public CodingSwampException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract int getStatusCode();

    public void addValidation(String fieldName, String message) {
        validation.put(fieldName, message);
    }
}
