package com.study.codingswamp.auth.service;

import com.study.codingswamp.auth.service.request.MailAuthenticationRequest;
import com.study.codingswamp.auth.service.response.MailAuthenticationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MailServiceTest {

    @Autowired
    private MailService mailService;

    @Test
    @DisplayName("메일을 주면 메일을 발송하고 인증번호를 생성한다.")
    void sendMaile() {
        // given
        MailAuthenticationRequest request = new MailAuthenticationRequest("seediu95@gmail.com");

        // when
        MailAuthenticationResponse response = mailService.sendEmail(request);

        // then
        assertThat(response.getAuthCode()).isNotNull();
    }
}