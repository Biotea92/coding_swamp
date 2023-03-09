package com.study.codingswamp.domain.study.repository;

import com.study.codingswamp.domain.study.dto.request.CursorRequest;
import com.study.codingswamp.domain.study.entity.Review;

import java.util.List;

public interface ReviewRepositoryCustom {

    List<Review> findAllByLessThanIdAndStudyId(CursorRequest cursorRequest, Long studyId);
    void deleteByStudyId(Long studyId);
}
