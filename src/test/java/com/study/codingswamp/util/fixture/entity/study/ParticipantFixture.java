package com.study.codingswamp.util.fixture.entity.study;

import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.study.entity.Participant;
import com.study.codingswamp.domain.study.entity.Study;

import java.time.LocalDate;

public class ParticipantFixture {

    public static Participant create(Member member, Study study) {
        return new Participant(study, member, LocalDate.now());
    }
}
