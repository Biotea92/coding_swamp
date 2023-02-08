package com.study.codingswamp.study.domain;

import com.study.codingswamp.member.domain.Member;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

import static javax.persistence.FetchType.LAZY;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class Participant {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(updatable = false, nullable = false)
    private LocalDate participationDate;
}
