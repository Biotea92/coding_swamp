package com.study.codingswamp.study.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;
import java.time.LocalDate;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Applicant {

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Lob
    private String reasonForApplication;

    @Column(updatable = false, nullable = false)
    private LocalDate applicantDate;
}
