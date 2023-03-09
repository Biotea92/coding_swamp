package com.study.codingswamp.util.fixture.entity.study;

import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.study.entity.Review;
import com.study.codingswamp.domain.study.entity.Study;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReviewFixture {

    public static List<Review> createReviews(Member member, Study study) {
        return IntStream.range(0, 100)
                .mapToObj(i -> new Review("리뷰 내용입니다" + i, member, study))
                .collect(Collectors.toList());
    }
}
