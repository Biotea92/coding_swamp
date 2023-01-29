package com.study.codingswamp.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MailServiceTest {

    @Autowired
    MailService mailService;

    @Test
    @DisplayName("메일을 주면 메일을 쏘고 인증번호를 생성한다.")
    void sendMaile() {
        // given
        MailAuthenticationDto mailAuthenticationDto = new MailAuthenticationDto();
        mailAuthenticationDto.setEmail("seediu95@gmail.com");

        // when
        mailService.sendEmail(mailAuthenticationDto);

        // then
        assertThat(mailAuthenticationDto.getAuthCode()).isNotNull();
    }
}