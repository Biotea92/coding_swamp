package com.study.codingswamp.auth.controller;

import com.study.codingswamp.auth.service.MailAuthenticationDto;
import com.study.codingswamp.auth.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final MailService mailService;

    @PostMapping("/email")
    public MailAuthenticationDto authenticateMail(@Validated @RequestBody MailAuthenticationDto request) {
        return mailService.sendEmail(request);
    }
}
