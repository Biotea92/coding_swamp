package com.study.codingswamp.domain.study.entity;

import com.study.codingswamp.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

import static javax.persistence.FetchType.LAZY;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Applicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "applicant_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Lob
    @Column(updatable = false, nullable = false)
    private String reasonForApplication;

    @Column(updatable = false, nullable = false)
    private LocalDate applicantDate;

    public Applicant(Study study, Member member, String reasonForApplication, LocalDate applicantDate) {
        this.study = study;
        this.member = member;
        this.reasonForApplication = reasonForApplication;
        this.applicantDate = applicantDate;
    }
}
