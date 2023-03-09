package com.study.codingswamp.domain.study.service;

import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.member.repository.MemberRepository;
import com.study.codingswamp.domain.study.dto.request.CursorRequest;
import com.study.codingswamp.domain.study.dto.request.ReviewRequest;
import com.study.codingswamp.domain.study.dto.response.PageCursor;
import com.study.codingswamp.domain.study.dto.response.ParticipantResponse;
import com.study.codingswamp.domain.study.dto.response.ReviewResponse;
import com.study.codingswamp.domain.study.entity.Review;
import com.study.codingswamp.domain.study.entity.Study;
import com.study.codingswamp.domain.study.repository.ReviewRepository;
import com.study.codingswamp.domain.study.repository.StudyRepository;
import com.study.codingswamp.exception.NotFoundException;
import com.study.codingswamp.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    public PageCursor<ReviewResponse> getReviews(Long memberId, Long studyId, CursorRequest cursorRequest) {
        Member member = findMember(memberId);
        Study study = findStudy(studyId);
        study.isParticipant(member);

        List<Review> reviews = reviewRepository.findAllByLessThanIdAndStudyId(cursorRequest, studyId);

        Long nextKey = reviews.stream()
                .mapToLong(Review::getId)
                .min().orElse(CursorRequest.NONE_KEY);

        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(review -> ReviewResponse.builder()
                        .reviewId(review.getId())
                        .createdAt(review.getCreatedAt())
                        .content(review.getContent())
                        .participantResponse(new ParticipantResponse(review.getMember()))
                        .build()
                ).collect(Collectors.toList());

        return new PageCursor<>(cursorRequest.next(nextKey), reviewResponses);
    }

    @Transactional
    public void edit(Long memberId, Long reviewId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(NotFoundException::new);
        validateReviewWriter(memberId, review);
        review.updateContent(request.getContent());
    }

    private void validateReviewWriter(Long memberId, Review review) {
        if (review.getMember() != findMember(memberId)) {
            throw new UnauthorizedException();
        }
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
