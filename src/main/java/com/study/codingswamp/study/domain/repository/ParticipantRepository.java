package com.study.codingswamp.study.domain.repository;

import com.study.codingswamp.study.domain.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
}
