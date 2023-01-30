package com.study.codingswamp.auth.service.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MailAuthenticationRequest {

    private String email;
}
