package com.study.codingswamp.member.service.exception;

import com.study.codingswamp.common.exception.UnauthorizedException;

public class MemberUnauthorizedException extends UnauthorizedException {

    public MemberUnauthorizedException(String message) {
        super(message);
    }
}
