package com.study.codingswamp.study.domain.repository;

import com.study.codingswamp.study.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
