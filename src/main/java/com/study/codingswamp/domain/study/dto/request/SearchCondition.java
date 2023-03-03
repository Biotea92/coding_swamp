package com.study.codingswamp.domain.study.dto.request;

import com.study.codingswamp.exception.InvalidRequestException;
import com.study.codingswamp.domain.study.entity.StudyType;
import lombok.Getter;
import lombok.ToString;

import static java.lang.Math.max;
import static java.lang.Math.min;

@Getter
@ToString
public class SearchCondition {

    private static final int MAX_SIZE = 100;

    private final Integer page;
    private final Integer size;
    private final String title;
    private final String studyType;
    private final String tag;

    public SearchCondition(Integer page, Integer size, String title, String studyType, String tag) {
        this.page = page == null ? 1 : page;
        this.size = size == null ? 8 : size;
        this.title = title;
        this.studyType = studyType;
        this.tag = tag;
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

    public StudyType mapToStudyType() {
        if (this.studyType == null) {
            return null;
        }
        if (this.studyType.equals("STUDY")) {
            return StudyType.STUDY;
        } else if (this.studyType.equals("MOGAKKO")) {
            return StudyType.MOGAKKO;
        }
        throw new InvalidRequestException("studyType", "STUDY 또는 MOGAKKO 이어야 합니다.");
    }
}
