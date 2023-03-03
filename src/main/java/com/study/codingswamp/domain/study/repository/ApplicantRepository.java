package com.study.codingswamp.domain.study.repository;

import com.study.codingswamp.domain.study.entity.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicantRepository extends JpaRepository<Applicant, Long>{
}
