package com.study.codingswamp.domain.study.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.codingswamp.domain.study.dto.request.CursorRequest;
import com.study.codingswamp.domain.study.entity.Review;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.study.codingswamp.domain.study.entity.QReview.review;

@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Review> findAllByLessThanIdAndStudyId(CursorRequest cursorRequest, Long studyId) {
        return jpaQueryFactory.selectFrom(review)
                .where(
                        review.study.id.eq(studyId),
                        lessThanReviewId(cursorRequest)
                )
                .orderBy(review.createdAt.desc())
                .limit(cursorRequest.getSize())
                .fetch();
    }

    private BooleanExpression lessThanReviewId(CursorRequest cursorRequest) {
        if (cursorRequest.hasKey()) {
            return review.id.lt(cursorRequest.getKey());
        }
        return null;
    }
}
