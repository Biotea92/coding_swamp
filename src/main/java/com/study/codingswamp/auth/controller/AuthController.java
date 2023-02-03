package com.study.codingswamp.auth.controller;

import com.study.codingswamp.auth.config.AuthenticatedMember;
import com.study.codingswamp.auth.config.Login;
import com.study.codingswamp.auth.service.AuthService;
import com.study.codingswamp.auth.service.MailService;
import com.study.codingswamp.auth.service.MemberPayload;
import com.study.codingswamp.auth.service.request.CommonLoginRequest;
import com.study.codingswamp.auth.service.request.MailAuthenticationRequest;
import com.study.codingswamp.auth.service.response.AccessTokenResponse;
import com.study.codingswamp.auth.service.response.MailAuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final MailService mailService;
    private final AuthService authService;

    @PostMapping("/email")
    public MailAuthenticationResponse authenticateMail(@Validated @RequestBody MailAuthenticationRequest request) {
        return mailService.sendEmail(request);
    }

    @PostMapping("/login/{loginType}")
    public AccessTokenResponse login(@PathVariable String loginType, @RequestBody CommonLoginRequest request) {
        return authService.login(request);
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
}
