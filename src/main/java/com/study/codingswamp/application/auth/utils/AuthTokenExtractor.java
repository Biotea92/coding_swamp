package com.study.codingswamp.application.auth.utils;

import com.study.codingswamp.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenExtractor {

    public String extractToken(String authorizationHeader, String tokenType) {
        if (authorizationHeader == null) {
            throw new UnauthorizedException("token", "토큰을 찾을 수 없습니다.");
        }

        String[] splitHeaders = authorizationHeader.split(" ");

        if (splitHeaders.length != 2 || !splitHeaders[0].equalsIgnoreCase(tokenType)) {
            throw new UnauthorizedException("token", "토큰 형식이 맞지 않습니다.");
        }
        return splitHeaders[1];
    }
}
