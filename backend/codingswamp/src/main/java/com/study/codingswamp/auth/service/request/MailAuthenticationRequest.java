package com.study.codingswamp.auth.service.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
public class MailAuthenticationRequest {

    @NotBlank
    @Email
    private String email;
}
