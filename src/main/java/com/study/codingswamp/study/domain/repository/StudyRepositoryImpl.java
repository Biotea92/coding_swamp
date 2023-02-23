package com.study.codingswamp.study.domain.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.study.domain.Study;
import com.study.codingswamp.study.domain.StudyStatus;
import com.study.codingswamp.study.domain.StudyType;
import com.study.codingswamp.study.service.request.SearchCondition;
import com.study.codingswamp.study.service.request.StudiesPageableRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

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
    public List<Study> getSearchStudies(SearchCondition condition) {
        return jpaQueryFactory.selectFrom(study)
                .where(
                        likeTitle(condition.getTitle()),
                        eqStudyType(condition.mapToStudyType()),
                        likeTag(condition.getTag())
                )
                .limit(condition.getSize())
                .offset(condition.getOffset())
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
    public Long getCount(SearchCondition condition) {
        return jpaQueryFactory.select(study.count())
                .from(study)
                .where(
                        likeTitle(condition.getTitle()),
                        eqStudyType(condition.mapToStudyType()),
                        likeTag(condition.getTag())
                )
                .fetchOne();
    }

    @Override
    public List<Study> findMyApplies(Member member) {
        return jpaQueryFactory.selectFrom(study)
                .leftJoin(applicant)
                .on(study.id.eq(applicant.study.id))
                .where(applicant.member.id.eq(member.getId()))
                .orderBy(applicant.applicantDate.desc())
                .fetchJoin().fetch();
    }

    @Override
    public List<Study> findMyParticipates(Member member) {
        return jpaQueryFactory.selectFrom(study)
                .leftJoin(participant)
                .on(study.id.eq(participant.study.id))
                .where(participant.member.id.eq(member.getId()))
                .orderBy(participant.participationDate.desc())
                .fetchJoin().fetch();
    }

    @Override
    public List<Study> findStudyStatusIsNotCompleted() {
        return jpaQueryFactory.selectFrom(study)
                .where(study.studyStatus.ne(StudyStatus.COMPLETION))
                .fetch();
    }

    private BooleanExpression likeTitle(String title) {
        if (StringUtils.hasText(title)) {
            return study.title.like("%" + title + "%");
        }
        return  null;
    }

    private BooleanExpression eqStudyType(StudyType studyType) {
        if (studyType != null) {
            return study.studyType.eq(studyType);
        }
        return  null;
    }

    private BooleanExpression likeTag(String tag) {
        if (StringUtils.hasText(tag)) {
            return study.tags.any().tagText.containsIgnoreCase(tag);
        }
        return  null;
    }
}
