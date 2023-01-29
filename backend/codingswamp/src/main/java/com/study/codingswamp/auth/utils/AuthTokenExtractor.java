package com.study.codingswamp.auth.utils;

import com.study.codingswamp.auth.exception.TokenInvalidFormatException;
import com.study.codingswamp.auth.exception.TokenNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenExtractor {

    public String extractToken(String authorizationHeader, String tokenType) {
        if (authorizationHeader == null) {
            throw new TokenNotFoundException();
        }

        String[] splitHeaders = authorizationHeader.split(" ");

        if (splitHeaders.length != 2 || !splitHeaders[0].equalsIgnoreCase(tokenType)) {
            throw new TokenInvalidFormatException();
        }
        return splitHeaders[1];
    }
}
