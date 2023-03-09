package com.study.codingswamp.domain.study.service;

import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.member.repository.MemberRepository;
import com.study.codingswamp.domain.study.dto.request.CursorRequest;
import com.study.codingswamp.domain.study.dto.request.ReviewRequest;
import com.study.codingswamp.domain.study.dto.response.PageCursor;
import com.study.codingswamp.domain.study.dto.response.ReviewResponse;
import com.study.codingswamp.domain.study.entity.Participant;
import com.study.codingswamp.domain.study.entity.Review;
import com.study.codingswamp.domain.study.entity.Study;
import com.study.codingswamp.domain.study.repository.ParticipantRepository;
import com.study.codingswamp.domain.study.repository.ReviewRepository;
import com.study.codingswamp.domain.study.repository.StudyRepository;
import com.study.codingswamp.util.fixture.dto.study.ReviewRequestFixture;
import com.study.codingswamp.util.fixture.entity.member.MemberFixture;
import com.study.codingswamp.util.fixture.entity.study.ParticipantFixture;
import com.study.codingswamp.util.fixture.entity.study.ReviewFixture;
import com.study.codingswamp.util.fixture.entity.study.StudyFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReviewServiceTest {

    @Autowired
    ReviewService reviewService;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    StudyRepository studyRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    ParticipantRepository participantRepository;

    @BeforeEach
    void clear() {
        jdbcTemplate.update("alter table member auto_increment= ?", 1);
        jdbcTemplate.update("alter table study auto_increment= ?", 1);
        jdbcTemplate.update("alter table review auto_increment= ?", 1);
    }

    @Test
    @DisplayName("리뷰를 등록한다.")
    void register() {
        // given
        Member member = MemberFixture.create();
        memberRepository.save(member);

        Study study = StudyFixture.createEasy(member);
        studyRepository.save(study);

        Participant participant = ParticipantFixture.create(member, study);
        study.initParticipants(participant);
        participantRepository.save(participant);

        // when
        ReviewRequest reviewRequest = ReviewRequestFixture.create();
        reviewService.register(member.getId(), study.getId(), reviewRequest);

        // then
        Review review = reviewRepository.findById(1L).orElseThrow();
        assertThat(review.getContent()).isEqualTo(reviewRequest.getContent());
        assertThat(review.getMember()).isEqualTo(member);
        assertThat(review.getStudy()).isEqualTo(study);
    }

    @Test
    @DisplayName("리뷰를 조회한다.")
    void getReviews() {
        // given
        Member member = MemberFixture.create();
        memberRepository.save(member);

        Study study = StudyFixture.createEasy(member);
        studyRepository.save(study);

        Participant participant = ParticipantFixture.create(member, study);
        study.initParticipants(participant);
        participantRepository.save(participant);

        List<Review> reviews = ReviewFixture.createReviews(member, study);
        reviewRepository.saveAll(reviews);


        // when
        PageCursor<ReviewResponse> response = reviewService.getReviews(member.getId(), study.getId(), new CursorRequest(null, null));

        // then
        assertThat(response.getBody().size()).isEqualTo(8);
        assertThat(response.getBody().get(0).getReviewId()).isEqualTo(100L);
        assertThat(response.getNextCursorRequest().getKey()).isEqualTo(93L);
    }

    @Test
    @DisplayName("리뷰를 조회한다. 다음 키")
    void getReviewsNextKey() {
        // given
        Member member = MemberFixture.create();
        memberRepository.save(member);

        Study study = StudyFixture.createEasy(member);
        studyRepository.save(study);

        Participant participant = ParticipantFixture.create(member, study);
        study.initParticipants(participant);
        participantRepository.save(participant);

        List<Review> reviews = ReviewFixture.createReviews(member, study);
        reviewRepository.saveAll(reviews);


        // when
        PageCursor<ReviewResponse> response = reviewService.getReviews(member.getId(), study.getId(), new CursorRequest(93L, null));

        // then
        assertThat(response.getBody().get(0).getReviewId()).isEqualTo(92L);
        assertThat(response.getNextCursorRequest().getKey()).isEqualTo(85L);
    }

    @Test
    @DisplayName("리뷰를 조회한다. 마지막 키")
    void getReviewsLastKey() {
        // given
        Member member = MemberFixture.create();
        memberRepository.save(member);

        Study study = StudyFixture.createEasy(member);
        studyRepository.save(study);

        Participant participant = ParticipantFixture.create(member, study);
        study.initParticipants(participant);
        participantRepository.save(participant);

        List<Review> reviews = ReviewFixture.createReviews(member, study);
        reviewRepository.saveAll(reviews);


        // when
        PageCursor<ReviewResponse> response = reviewService.getReviews(member.getId(), study.getId(), new CursorRequest(-1L, null));

        // then
        assertThat(response.getBody().get(0).getReviewId()).isEqualTo(100L);
    }
}