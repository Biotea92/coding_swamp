package com.study.codingswamp.study.domain.repository;

import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.study.domain.Study;
import com.study.codingswamp.study.service.request.StudiesPageableRequest;

import java.util.List;

public interface StudyRepositoryCustom {

    List<Study> getStudies(StudiesPageableRequest request);

    Long getCount();

    List<Study> findMyAppliedStudy(Member member);
}
