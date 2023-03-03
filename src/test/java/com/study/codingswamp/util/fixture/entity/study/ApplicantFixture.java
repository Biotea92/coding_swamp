package com.study.codingswamp.util.fixture.entity.study;

import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.study.entity.Applicant;
import com.study.codingswamp.domain.study.entity.Study;

import java.time.LocalDate;

public class ApplicantFixture {

    public static Applicant create(Study study, Member member) {
        return new Applicant(study, member, "지원동기", LocalDate.now());
    }
}
