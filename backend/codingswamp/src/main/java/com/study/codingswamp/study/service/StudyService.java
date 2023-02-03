package com.study.codingswamp.study.service;

import com.study.codingswamp.auth.service.MemberPayload;
import com.study.codingswamp.common.exception.NotFoundException;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import com.study.codingswamp.study.domain.Study;
import com.study.codingswamp.study.domain.repository.StudyRepository;
import com.study.codingswamp.study.service.request.StudyCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final MemberRepository memberRepository;
    public Study createStudy(MemberPayload memberPayload, StudyCreateRequest request) {
        Member member = memberRepository.findById(memberPayload.getId())
                .orElseThrow(() -> new NotFoundException("member", "사용자를 찾을 수 없습니다."));

        Long ownerId = member.getId();
        Study study = request.mapToStudy(ownerId);
        return studyRepository.save(study);
    }
}
