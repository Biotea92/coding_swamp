package com.study.codingswamp.member.domain.repository;

import com.study.codingswamp.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
