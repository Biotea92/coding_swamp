package com.study.codingswamp.study.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.LocalDate;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Participant {

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(updatable = false, nullable = false)
    private LocalDate participationDate;
}
