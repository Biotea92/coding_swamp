package com.study.codingswamp.auth.utils;

import com.study.codingswamp.auth.exception.TokenInvalidFormatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class AuthTokenExtractorTest {

    @Autowired
    AuthTokenExtractor authTokenExtractor;

    private static final String tokenType = "Bearer";

    @Test
    @DisplayName("정상토큰 추출이 추출되어야한다.")
    void tokenExtract() {
        // given
        String authorizationHeader = "Bearer sdfsfdas.asdfasdf.asdfadsaf";

        // when
        String token = authTokenExtractor.extractToken(authorizationHeader, tokenType);

        // then
        assertThat(token).isEqualTo("sdfsfdas.asdfasdf.asdfadsaf");
    }

    @Test
    @DisplayName("정상토큰이 아니면 예외가 발생한다.")
    void tokenExtractException() {
        // given
        String authorizationHeader1 = "tokenTypeError sdfsfdas.asdfasdf.asdfadsaf";
        String authorizationHeader2 = "tokenTypesdfsfdas.asdfasdf.asdfadsaf";

        // expected
        assertThrows(
                TokenInvalidFormatException.class,
                () -> authTokenExtractor.extractToken(authorizationHeader1, tokenType)
        );
        assertThrows(
                TokenInvalidFormatException.class,
                () -> authTokenExtractor.extractToken(authorizationHeader2, tokenType)
        );
    }
}