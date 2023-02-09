package com.study.codingswamp.member.service.request;

import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
public class MemberEditRequest {

    @NotBlank
    @Length(min = 3, max = 20 , message = "최소 3자 이상 최대 20자 이하 이어야 합니다.")
    @Pattern(
            regexp = "^[0-9a-zA-Z가-힣_]*$",
            message = "한글, 숫자, 영어, _ 만 가능합니다."
    )
    private final String username;

    @URL
    private final String profileUrl;

    private final MultipartFile imageFile;

    @Builder
    public MemberEditRequest(String username, String profileUrl, MultipartFile imageFile) {
        this.username = username;
        this.profileUrl = profileUrl;
        this.imageFile = imageFile;
    }
}
