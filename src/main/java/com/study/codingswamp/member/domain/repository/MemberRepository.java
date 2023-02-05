package com.study.codingswamp.member.domain.repository;

import com.study.codingswamp.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String Email);
    Optional<Member> findByGithubId(Long githubId);
}
