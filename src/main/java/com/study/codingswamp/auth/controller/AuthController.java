package com.study.codingswamp.auth.controller;

import com.study.codingswamp.auth.config.AuthenticatedMember;
import com.study.codingswamp.auth.config.Login;
import com.study.codingswamp.auth.oauth.GithubOauthClient;
import com.study.codingswamp.auth.oauth.response.GithubProfileResponse;
import com.study.codingswamp.auth.service.AuthService;
import com.study.codingswamp.auth.service.MailService;
import com.study.codingswamp.auth.service.MemberPayload;
import com.study.codingswamp.auth.service.request.CommonLoginRequest;
import com.study.codingswamp.auth.service.request.MailAuthenticationRequest;
import com.study.codingswamp.auth.service.response.AccessTokenResponse;
import com.study.codingswamp.auth.service.response.MailAuthenticationResponse;
import com.study.codingswamp.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final MailService mailService;
    private final AuthService authService;
    private final GithubOauthClient githubOauthClient;
    private static final String AUTH_CODE = "authCode";

    @PostMapping("/email")
    public ResponseEntity<Void> authenticateSendMail(
            @Validated @RequestBody MailAuthenticationRequest mailAuthenticationRequest,
            HttpServletRequest request
    ) {
        MailAuthenticationResponse response = mailService.sendEmail(mailAuthenticationRequest);
        setSession(request, response);
        return ResponseEntity.created(URI.create("/api/auth/email/confirm")).build();
    }

    @PostMapping("/email/confirm")
    public ResponseEntity<Void> authenticatedMailConfirm(HttpServletRequest request) {
        getSessionAndAuthenticate(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login/common")
    public AccessTokenResponse login(@RequestBody CommonLoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/login/github")
    public AccessTokenResponse githubLogin(String code) {
        GithubProfileResponse profileResponse = githubOauthClient.getProfile(code);
        return authService.githubLogin(profileResponse);
    }

    @Login
    @PostMapping("/refresh")
    public AccessTokenResponse refresh(@AuthenticatedMember MemberPayload memberPayload) {
        return authService.refreshToken(memberPayload);
    }

    @GetMapping("/foo")
    public String foo() {
        return "ok";
    }

    private static void setSession(HttpServletRequest request, MailAuthenticationResponse response) {
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(300);
        session.setAttribute(AUTH_CODE, response.getAuthCode());
    }

    private static void getSessionAndAuthenticate(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new UnauthorizedException(AUTH_CODE, "세션이 만료되었습니다.");
        }
        String authCode = (String) session.getAttribute(AUTH_CODE);
        String requestAuthCode = request.getParameter(AUTH_CODE);
        if (!authCode.equals(requestAuthCode)) {
            throw new UnauthorizedException(AUTH_CODE, "인증번호가 일치하지않습니다.");
        }
    }
}
