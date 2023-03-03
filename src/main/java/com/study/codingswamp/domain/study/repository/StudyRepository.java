package com.study.codingswamp.domain.study.repository;

import com.study.codingswamp.domain.study.entity.Study;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryCustom {
}
