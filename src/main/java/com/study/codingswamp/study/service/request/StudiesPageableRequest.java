package com.study.codingswamp.study.service.request;

import lombok.Getter;

import static java.lang.Math.*;

@Getter
public class StudiesPageableRequest {

    private static final int MAX_SIZE = 100;

    private final Integer page;
    private final Integer size;

    public StudiesPageableRequest(Integer page, Integer size) {
        this.page = page == null ? 1 : page;
        this.size = size == null ? 8 : size;
    }

    public long getOffset() {
        return (long) (max(1, page) - 1) * getValidatedSize();
    }

    public int getTotalPage(Long totalCount) {
        return (int) Math.ceil(totalCount.doubleValue() / (double) getValidatedSize());
    }

    private int getValidatedSize() {
        return min(size, MAX_SIZE);
    }
}
