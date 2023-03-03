package com.study.codingswamp.util.fixture.domain;

import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.member.entity.Role;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import java.time.LocalDateTime;

import static org.jeasy.random.FieldPredicates.*;

public class MemberFixture {

    static public Member create() {
        var idPredicate = named("id")
                .and(ofType(Long.class))
                .and(inClass(Member.class));

        var rolePredicate = named("role")
                .and(ofType(Role.class))
                .and(inClass(Member.class));

        var joinedAtPredicate = named("joinedAt")
                .and(ofType(LocalDateTime.class))
                .and(inClass(Member.class));

        var param = new EasyRandomParameters()
                .excludeField(idPredicate)
                .excludeField(joinedAtPredicate)
                .randomize(rolePredicate, () -> Role.USER);

        return new EasyRandom(param).nextObject(Member.class);
    }
}
