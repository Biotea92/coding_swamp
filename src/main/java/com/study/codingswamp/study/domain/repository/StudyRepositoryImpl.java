package com.study.codingswamp.study.domain.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.study.domain.Study;
import com.study.codingswamp.study.service.request.StudiesPageableRequest;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.study.codingswamp.study.domain.QApplicant.applicant;
import static com.study.codingswamp.study.domain.QParticipant.participant;
import static com.study.codingswamp.study.domain.QStudy.study;

@RequiredArgsConstructor
public class StudyRepositoryImpl implements StudyRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Study> getStudies(StudiesPageableRequest request) {
         return jpaQueryFactory.selectFrom(study)
                .limit(request.getSize())
                .offset(request.getOffset())
                .orderBy(study.id.desc())
                .fetch();
    }

    @Override
    public Long getCount() {
        return jpaQueryFactory.select(study.count())
                .from(study)
                .fetchOne();
    }

    @Override
    public List<Study> findMyApplies(Member member) {
        return jpaQueryFactory.selectFrom(study)
                .leftJoin(study.applicants, applicant)
                .where(applicant.member.id.eq(member.getId()))
                .orderBy(applicant.applicantDate.desc())
                .fetchJoin().fetch();
    }

    @Override
    public List<Study> findMyParticipates(Member member) {
        return jpaQueryFactory.selectFrom(study)
                .leftJoin(study.participants, participant)
                .where(participant.member.id.eq(member.getId()))
                .orderBy(participant.participationDate.desc())
                .fetchJoin().fetch();
    }
}
