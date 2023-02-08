package com.study.codingswamp.study.domain;

import com.study.codingswamp.member.domain.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

import static javax.persistence.FetchType.LAZY;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Applicant {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Lob
    @Column(updatable = false, nullable = false)
    private String reasonForApplication;

    @Column(updatable = false, nullable = false)
    private LocalDate applicantDate;
}
