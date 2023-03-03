package com.study.codingswamp.util.fixture.dto;

import com.study.codingswamp.domain.study.dto.request.ReviewRequest;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

public class ReviewRequestFixture {

    static public ReviewRequest create() {
        var param = new EasyRandomParameters();
        return new EasyRandom(param).nextObject(ReviewRequest.class);
    }
}
