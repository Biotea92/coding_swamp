package com.study.codingswamp.auth.token;

import com.study.codingswamp.auth.service.MemberPayload;
import com.study.codingswamp.auth.utils.AuthTokenExtractor;
import com.study.codingswamp.member.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenProviderTest {

    private final TokenProvider tokenProvider = new TokenProvider(
            new AuthTokenExtractor(),
            "testabcccseesfsadfajsalkfasdfjlkasf120309192039dfjkasjdddsfsjalsdf",
            3600000
    );

    @DisplayName("토큰을 생성한다.")
    @Test
    void createToken() {
        // given
        Long memberId = 1L;

        // when
        String token = tokenProvider.createAccessToken(memberId, Role.USER);

        // then
        assertThat(token).isNotNull();
    }

    @DisplayName("유효한 토큰인 경우")
    @Test
    void validToken() {
        // given
        String token = tokenProvider.createAccessToken(1L, Role.USER);
        String authorizationHeader = "Bearer " + token;

        // expected
        assertThat(tokenProvider.isValidToken(authorizationHeader)).isTrue();
    }

    @DisplayName("유효기간이 지난 토큰인 경우")
    @Test
    void invalidToken() {
        // given
        TokenProvider tokenProvider = new TokenProvider(
                new AuthTokenExtractor(),
                "testabcccseesfsadfajsalkfasdfjlkasf120309192039dfjkasjdddsfsjalsdf",
                0
        );
        String token = tokenProvider.createAccessToken(1L, Role.USER);
        String authorizationHeader = "Bearer " + token;

        // expected
        assertThat(tokenProvider.isValidToken(authorizationHeader)).isFalse();
    }

    @DisplayName("토큰의 형식이 틀린 경우")
    @Test
    void invalidFormatToken() {
        // given
        String authorizationHeader = "Bearer " + "notToken";

        // expected
        assertThat(tokenProvider.isValidToken(authorizationHeader)).isFalse();
    }

    @DisplayName("토큰의 시크릿 키가 틀린 경우")
    @Test
    void notMySecretKey() {
        // given
        TokenProvider notMyTokenProvider = new TokenProvider(
                new AuthTokenExtractor(),
                "notMySecretKeyabcccseesfsadfajsalkfasdfjlkasf120309192039dfjkasjdddsfsjalsdf",
                36000000
        );
        String token = notMyTokenProvider.createAccessToken(1L, Role.USER);
        String authorizationHeader = "Bearer " + token;

        // expected
        assertThat(tokenProvider.isValidToken(authorizationHeader)).isFalse();
    }

    @DisplayName("토큰의 payload를 가져온다.")
    @Test
    void getPayload() {
        // given
        String token = tokenProvider.createAccessToken(1L, Role.USER);
        String authorizationHeader = "Bearer " + token;

        // when
        MemberPayload payload = tokenProvider.getPayload(authorizationHeader);

        // then
        assertThat(payload).isEqualTo(new MemberPayload(1L, Role.USER));
    }

    // TODO 예외테스트 추가

}