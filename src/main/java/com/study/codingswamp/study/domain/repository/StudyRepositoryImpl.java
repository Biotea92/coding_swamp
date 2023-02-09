package com.study.codingswamp.study.domain.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.codingswamp.study.domain.Study;
import com.study.codingswamp.study.service.request.StudiesPageableRequest;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
}
