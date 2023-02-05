package com.study.codingswamp.member.service.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberEditRequest {

    @NotBlank
    @Length(min = 3, message = "최소 3자 이상이어야 합니다.")
    private String username;

    private String profileUrl;

    private MultipartFile imageFile;
}
