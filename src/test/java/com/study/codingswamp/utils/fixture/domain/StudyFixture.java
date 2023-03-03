package com.study.codingswamp.utils.fixture.domain;

import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.study.entity.Study;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import java.util.Set;

import static org.jeasy.random.FieldPredicates.*;

public class StudyFixture {

    static public Study create(Member member) {

        var idPredicate = named("id")
                .and(ofType(Long.class))
                .and(inClass(Study.class));

        var ownerPredicate = named("owner")
                .and(ofType(Member.class))
                .and(inClass(Study.class));

        var participantsPredicate = named("participants")
                .and(ofType(Set.class))
                .and(inClass(Study.class));

        var applicantsPredicate = named("applicants")
                .and(ofType(Set.class))
                .and(inClass(Study.class));

        var param = new EasyRandomParameters()
                .randomize(ownerPredicate, () -> member)
                .excludeField(idPredicate)
                .excludeField(participantsPredicate)
                .excludeField(applicantsPredicate);

        return new EasyRandom(param).nextObject(Study.class);
    }
}
