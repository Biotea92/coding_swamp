package com.study.codingswamp.utils.fixture.dto;

import com.study.codingswamp.study.service.request.ReviewRequest;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

public class ReviewRequestFixture {

    static public ReviewRequest create() {
        var param = new EasyRandomParameters();
        return new EasyRandom(param).nextObject(ReviewRequest.class);
    }
}
