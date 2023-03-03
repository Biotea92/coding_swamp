package com.study.codingswamp.util.fixture.entity.study;

import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.study.entity.*;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.jeasy.random.FieldPredicates.*;

public class StudyFixture {

    public static Study createEasy(Member member) {
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

    public static Study create(Member member) {
        return Study.builder()
                .title("제목입니다.")
                .description("설명입니다.")
                .studyStatus(StudyStatus.PREPARING)
                .studyType(StudyType.STUDY)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .owner(member)
                .currentMemberCount(1)
                .maxMemberCount(30)
                .thumbnail("#00000")
                .applicants(new HashSet<>())
                .participants(new HashSet<>())
                .tags(List.of(new Tag("태그1"), new Tag("태그2")))
                .build();
    }

    public static List<Study> createStudies(Member member) {
        return IntStream.range(0, 20)
                .mapToObj(i -> {
                    Study study = Study.builder()
                            .title("제목입니다. " + i)
                            .description("설명입니다. " + i)
                            .studyStatus(StudyStatus.PREPARING)
                            .studyType(StudyType.STUDY)
                            .startDate(LocalDate.now().plusDays(1))
                            .endDate(LocalDate.now().plusDays(2))
                            .owner(member)
                            .currentMemberCount(1)
                            .maxMemberCount(30)
                            .thumbnail("#00000")
                            .applicants(new HashSet<>())
                            .participants(new HashSet<>())
                            .tags(List.of(new Tag("태그1"), new Tag("태그2")))
                            .build();
                    study.initParticipants(new Participant(study, member, LocalDate.now()));
                    return study;
                })
                .collect(Collectors.toList());
    }
}
