package com.study.codingswamp.domain.study.service;

import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.member.repository.MemberRepository;
import com.study.codingswamp.domain.study.entity.Participant;
import com.study.codingswamp.domain.study.entity.Review;
import com.study.codingswamp.domain.study.entity.Study;
import com.study.codingswamp.domain.study.repository.ParticipantRepository;
import com.study.codingswamp.domain.study.repository.ReviewRepository;
import com.study.codingswamp.domain.study.repository.StudyRepository;
import com.study.codingswamp.domain.study.dto.request.ReviewRequest;
import com.study.codingswamp.util.fixture.entity.member.MemberFixture;
import com.study.codingswamp.util.fixture.entity.study.ParticipantFixture;
import com.study.codingswamp.util.fixture.dto.study.ReviewRequestFixture;
import com.study.codingswamp.util.fixture.entity.study.StudyFixture;
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
}