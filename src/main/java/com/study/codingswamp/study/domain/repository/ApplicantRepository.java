package com.study.codingswamp.study.domain.repository;

import com.study.codingswamp.study.domain.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicantRepository extends JpaRepository<Applicant, Long>{
}
