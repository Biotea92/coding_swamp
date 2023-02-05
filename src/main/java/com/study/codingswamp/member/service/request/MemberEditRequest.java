package com.study.codingswamp.member.service.request;

import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;

@Getter
public class MemberEditRequest {

    @NotBlank
    @Length(min = 3, message = "최소 3자 이상이어야 합니다.")
    private final String username;

    private final String profileUrl;

    private final MultipartFile imageFile;

    @Builder
    public MemberEditRequest(String username, String profileUrl, MultipartFile imageFile) {
        this.username = username;
        this.profileUrl = profileUrl;
        this.imageFile = imageFile;
    }
}
