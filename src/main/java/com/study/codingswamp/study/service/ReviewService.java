package com.study.codingswamp.study.service;

import com.study.codingswamp.common.exception.NotFoundException;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import com.study.codingswamp.study.domain.Review;
import com.study.codingswamp.study.domain.Study;
import com.study.codingswamp.study.domain.repository.ReviewRepository;
import com.study.codingswamp.study.domain.repository.StudyRepository;
import com.study.codingswamp.study.service.request.ReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final MemberRepository memberRepository;
    private final StudyRepository studyRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public void register(Long memberId, Long studyId, ReviewRequest request) {
        Study findStudy = findStudy(studyId);
        Member member = findMember(memberId);
        findStudy.isParticipant(member);
        Review review = new Review(request.getContent(), member, findStudy);
        reviewRepository.save(review);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("member", "사용자를 찾을 수 없습니다."));
    }

    private Study findStudy(Long studyId) {
        return studyRepository.findById(studyId)
                .orElseThrow(() -> new NotFoundException("studyId", "스터디를 찾을 수 없습니다."));
    }
}
