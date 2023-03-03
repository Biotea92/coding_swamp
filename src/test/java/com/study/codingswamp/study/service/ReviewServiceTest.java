package com.study.codingswamp.study.service;

import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import com.study.codingswamp.study.domain.Participant;
import com.study.codingswamp.study.domain.Review;
import com.study.codingswamp.study.domain.Study;
import com.study.codingswamp.study.domain.repository.ParticipantRepository;
import com.study.codingswamp.study.domain.repository.ReviewRepository;
import com.study.codingswamp.study.domain.repository.StudyRepository;
import com.study.codingswamp.study.service.request.ReviewRequest;
import com.study.codingswamp.utils.fixture.domain.MemberFixture;
import com.study.codingswamp.utils.fixture.domain.ParticipantFixture;
import com.study.codingswamp.utils.fixture.dto.ReviewRequestFixture;
import com.study.codingswamp.utils.fixture.domain.StudyFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

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

        Study study = StudyFixture.create(member);
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
}