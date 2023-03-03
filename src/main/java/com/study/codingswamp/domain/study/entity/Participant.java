package com.study.codingswamp.domain.study.entity;

import com.study.codingswamp.domain.member.entity.Member;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

import static javax.persistence.FetchType.LAZY;

@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@EqualsAndHashCode
@Entity
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(updatable = false, nullable = false)
    private LocalDate participationDate;

    public Participant(Study study, Member member, LocalDate participationDate) {
        this.study = study;
        this.member = member;
        this.participationDate = participationDate;
    }
}
