package com.study.codingswamp.domain.member.dto.request;

import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
public class MemberSignupRequest {

    @NotBlank
    @Email
    private final String email;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@!%*#?&])[A-Za-z\\d$@!%*#?&]{8,}$",
            message = "최소 8자, 최소 하나의 문자, 하나의 숫자 및 하나의 특수 문자를 포함해야합니다."
    )
    private final String password;

    @NotBlank
    @Length(min = 3, max = 20 , message = "최소 3자 이상 최대 20자 이하 이어야 합니다.")
    @Pattern(
            regexp = "^[0-9a-zA-Z가-힣_]*$",
            message = "한글, 숫자, 영어, _ 만 가능합니다."
    )
    private final String username;

    private final MultipartFile imageFile;

    @Builder
    public MemberSignupRequest(String email, String password, String username, MultipartFile imageFile) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.imageFile = imageFile;
    }
}
