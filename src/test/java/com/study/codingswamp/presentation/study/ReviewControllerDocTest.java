package com.study.codingswamp.presentation.study;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.codingswamp.application.auth.token.TokenProvider;
import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.member.repository.MemberRepository;
import com.study.codingswamp.domain.study.dto.request.ReviewRequest;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.removeHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@Transactional
class ReviewControllerDocTest {

    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private StudyRepository studyRepository;
    @Autowired
    private ParticipantRepository participantRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint(), removeHeaders("Vary"))
                )
                .build();

        jdbcTemplate.update("alter table study auto_increment= ?", 1);
        jdbcTemplate.update("alter table member auto_increment= ?", 1);
    }

    @Test
    @DisplayName("리뷰등록시 리뷰가 등록된다.")
    void register() throws Exception {
        // given
        Member owner = memberRepository.save(MemberFixture.create(true));
        Study study = studyRepository.save(StudyFixture.create(owner));
        Participant participant = ParticipantFixture.create(owner, study);
        participantRepository.save(participant);
        study.initParticipants(participant);
        ReviewRequest reviewRequest = ReviewRequestFixture.create();

        String json = objectMapper.writeValueAsString(reviewRequest);
        String token = tokenProvider.createAccessToken(owner.getId(), owner.getRole());

        // expected
        mockMvc.perform(post("/api/study/{studyId}/review", study.getId())
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .header(AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isCreated())
                .andDo(document("review-register",
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("Bearer auth credentials")
                        ),
                        pathParameters(
                                parameterWithName("studyId").description("스터디 아이디 type(Long)")
                        ),
                        requestFields(
                                fieldWithPath("content").description("리뷰 내용")
                        )
                ));
    }

    @Test
    @DisplayName("리뷰를 조회한다.")
    void getReviews() throws Exception {
        // given
        Member member = MemberFixture.createGithubMember();
        memberRepository.save(member);

        Study study = StudyFixture.createEasy(member);
        studyRepository.save(study);

        Participant participant = ParticipantFixture.create(member, study);
        study.initParticipants(participant);
        participantRepository.save(participant);

        List<Review> reviews = ReviewFixture.createReviews(member, study);
        reviewRepository.saveAll(reviews);

        String token = tokenProvider.createAccessToken(member.getId(), member.getRole());

        // expected
        mockMvc.perform(get("/api/study/{studyId}/review", study.getId())
                        .param("key", "50")
                        .param("size", "8")
                        .header(AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andDo(document("review-inquiry",
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("Bearer auth credentials")
                        ),
                        pathParameters(
                                parameterWithName("studyId").description("스터디 아이디 type(Long)")
                        ),
                        requestParameters(
                                parameterWithName("key").description("next key, 키를 주지않거나 음수 입력시 첫 번째 반환"),
                                parameterWithName("size").description("리뷰 수, 값을 주지않으면 default 8")
                        ),
                        responseFields(
                                fieldWithPath("nextCursorRequest").description("다음 커서"),
                                fieldWithPath("nextCursorRequest.key").description("다음 커서 key, -1이 반환되면 마지막 커서"),
                                fieldWithPath("nextCursorRequest.size").description("다음 커서 size"),
                                fieldWithPath("body").description("리뷰 body"),
                                fieldWithPath("body[].participantResponse").description("참가자 정보"),
                                fieldWithPath("body[].participantResponse.memberId").description("참가자 id"),
                                fieldWithPath("body[].participantResponse.username").description("참가자 닉네임"),
                                fieldWithPath("body[].participantResponse.imageUrl").description("참가자 imageUrl"),
                                fieldWithPath("body[].participantResponse.profileUrl").description("참가자 profileUrl"),
                                fieldWithPath("body[].participantResponse.participationDate").description("참가일 현재 필요없으므로 null 반환 중"),
                                fieldWithPath("body[].reviewId").description("reviewId"),
                                fieldWithPath("body[].content").description("리뷰 내용"),
                                fieldWithPath("body[].createdAt").description("리뷰 등록일")
                        )
                ));
    }

    @Test
    @DisplayName("리뷰를 수정한다.")
    void edit() throws Exception {
        // given
        Member member = MemberFixture.create();
        memberRepository.save(member);

        Study study = StudyFixture.createEasy(member);
        studyRepository.save(study);

        Participant participant = ParticipantFixture.create(member, study);
        study.initParticipants(participant);
        participantRepository.save(participant);

        Review review = ReviewFixture.create(member, study);
        reviewRepository.save(review);

        String token = tokenProvider.createAccessToken(member.getId(), member.getRole());
        ReviewRequest reviewRequest = new ReviewRequest("리뷰 수정입니다.");
        String json = objectMapper.writeValueAsString(reviewRequest);

        // when
        // expected
        mockMvc.perform(put("/api/study/{studyId}/review/{reviewId}", study.getId(), review.getId())
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .header(AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isCreated())
                .andDo(document("review-edit",
                        requestHeaders(
                                headerWithName(AUTHORIZATION).description("Bearer auth credentials")
                        ),
                        pathParameters(
                                parameterWithName("studyId").description("스터디 아이디 type(Long)"),
                                parameterWithName("reviewId").description("리뷰 아이디 type(Long)")
                        ),
                        requestFields(
                                fieldWithPath("content").description("리뷰 내용")
                        )
                ));
    }
}