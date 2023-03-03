package com.study.codingswamp.utils.fixture.domain;

import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.study.domain.Participant;
import com.study.codingswamp.study.domain.Study;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import java.time.LocalDate;

import static org.jeasy.random.FieldPredicates.*;

public class ParticipantFixture {


    static public Participant create(Member member, Study study) {
        var idPredicate = named("id")
                .and(ofType(Long.class))
                .and(inClass(Participant.class));

        var participantDatePredicate = named("participationDate")
                .and(ofType(LocalDate.class))
                .and(inClass(Participant.class));

        var memberPredicate = named("member")
                .and(ofType(Member.class))
                .and(inClass(Participant.class));

        var studyPredicate = named("study")
                .and(ofType(Study.class))
                .and(inClass(Participant.class));

        var param = new EasyRandomParameters()
                .excludeField(idPredicate)
                .randomize(participantDatePredicate, LocalDate::now)
                .randomize(memberPredicate, () -> member)
                .randomize(studyPredicate, () -> study);

        return new EasyRandom(param).nextObject(Participant.class);
    }
}
