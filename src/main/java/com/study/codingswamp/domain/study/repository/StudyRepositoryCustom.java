package com.study.codingswamp.domain.study.repository;

import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.study.dto.request.SearchCondition;
import com.study.codingswamp.domain.study.entity.Study;
import com.study.codingswamp.domain.study.dto.request.StudiesPageableRequest;

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
