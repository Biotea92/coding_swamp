package com.study.codingswamp.domain.study.repository;

import com.study.codingswamp.domain.study.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
}
