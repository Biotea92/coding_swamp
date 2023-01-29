package com.study.codingswamp.member.service.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Setter
@Getter
@ToString
public class MemberSignupRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@!%*#?&])[A-Za-z\\d$@!%*#?&]{8,}$",
            message = "최소 8자, 최소 하나의 문자, 하나의 숫자 및 하나의 특수 문자를 포함해야합니다."
    )
    private String password;

    @NotBlank
    @Length(min = 3, message = "최소 3자 이상이어야 합니다.")
    private String username;

    private MultipartFile imageFile;

    @Builder
    public MemberSignupRequest(String email, String password, String username, MultipartFile imageFile) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.imageFile = imageFile;
    }
}
