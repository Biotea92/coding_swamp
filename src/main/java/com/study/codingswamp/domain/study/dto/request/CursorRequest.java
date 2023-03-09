package com.study.codingswamp.domain.study.dto.request;

import lombok.Getter;

import static java.lang.Math.min;

@Getter
public class CursorRequest {

    public static final Long NONE_KEY = -1L;
    private static final int MAX_SIZE = 100;

    private final Long key;
    private final Integer size;

    public CursorRequest(Long key, Integer size) {
        this.key = key;
        this.size = size == null ? 8 : min(size, MAX_SIZE);
    }

    public Boolean hasKey() {
        return key != null && !key.equals(NONE_KEY);
    }

    public CursorRequest next(Long key) {
        return new CursorRequest(key, getValidatedSize());
    }

    private int getValidatedSize() {
        return min(size, MAX_SIZE);
    }
}
