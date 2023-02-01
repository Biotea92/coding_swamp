package com.study.codingswamp.auth.controller;

import com.study.codingswamp.auth.config.Login;
import com.study.codingswamp.auth.service.MemberPayload;
import com.study.codingswamp.auth.token.TokenProvider;
import com.study.codingswamp.common.exception.ForbiddenException;
import com.study.codingswamp.common.exception.UnauthorizedException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@AllArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final TokenProvider tokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod) || getLoginAnnotation(handler) == null) {
            return true;
        }

        if (request.getHeader(HttpHeaders.AUTHORIZATION) != null) {
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (!tokenProvider.isValidToken(authorizationHeader)) {
                throw new UnauthorizedException("token", "만료된 토큰입니다.");
            }

            // 관리자가 필요할 때 auth.admin() == true
            Login auth = getLoginAnnotation(handler);
            if (auth != null && auth.admin() && isNotAdmin(request)) {
                throw new ForbiddenException("role", "관리자가 아닙니다.");
            }
            return true;
        }

        Login auth = getLoginAnnotation(handler);
        if (auth != null && auth.required()) {
            throw new UnauthorizedException("token", "토큰이 존재하지않습니다.");
        }

        return true;
    }

    private boolean isNotAdmin(HttpServletRequest request) {
        MemberPayload payload = tokenProvider.getPayload(request.getHeader(HttpHeaders.AUTHORIZATION));
        return !payload.isAdmin();
    }

    private Login getLoginAnnotation(Object handler) {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        return handlerMethod.getMethodAnnotation(Login.class);
    }
}
