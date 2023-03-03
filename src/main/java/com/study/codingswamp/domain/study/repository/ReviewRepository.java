package com.study.codingswamp.domain.study.repository;

import com.study.codingswamp.domain.study.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
