package com.study.codingswamp.util.fixture.entity.member;

import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.member.entity.Role;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import java.time.LocalDateTime;

import static org.jeasy.random.FieldPredicates.*;

public class MemberFixture {

    public static Member create() {
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

    public static Member create(String email, String encodedPassword, String username) {
        return new Member(email, encodedPassword, username, null);
    }

    public static Member create(boolean needImageUrl) {
        if (needImageUrl) {
            return new Member("abc@gmail.com", "1q2w3e4r!", "hong", "https://firebasestorage.googleapis.com/v0/b/coding-swamp.appspot.com/o/default_image%2Fcrocodile.png?alt=media");
        }
        return new Member("abc@gmail.com", "1q2w3e4r!", "hong", null);
    }

    public static Member createGithubMember() {
        return new Member("seediu95@gmail.com", 102938L, "seediu", "https//image", "https//profile");
    }
}
