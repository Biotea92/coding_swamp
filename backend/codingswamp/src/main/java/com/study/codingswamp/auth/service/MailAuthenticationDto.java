package com.study.codingswamp.auth.service;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class MailAuthenticationDto {

    @NotBlank
    @Email
    private String email;
    private String authCode;
}
