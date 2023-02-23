package com.study.codingswamp.study.domain.repository;

import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.study.domain.Study;
import com.study.codingswamp.study.service.request.SearchCondition;
import com.study.codingswamp.study.service.request.StudiesPageableRequest;

import java.util.List;

public interface StudyRepositoryCustom {

    List<Study> getStudies(StudiesPageableRequest request);

    List<Study> getSearchStudies(SearchCondition condition);

    Long getCount();

    Long getCount(SearchCondition condition);

    List<Study> findMyApplies(Member member);

    List<Study> findMyParticipates(Member member);

    List<Study> findStudyStatusIsNotCompleted();
}
