package com.study.codingswamp.application.auth.service.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MailAuthenticationRequest {

    @NotBlank
    @Email
    private String email;
}
