package com.study.codingswamp.study.domain.repository;

import com.study.codingswamp.study.domain.Study;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryCustom {
}
