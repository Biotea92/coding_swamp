package com.study.codingswamp.utils;

import com.study.codingswamp.auth.token.TokenProvider;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.Role;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import org.springframework.jdbc.core.JdbcTemplate;


public class TestUtil {

    public String saveMemberAndGetToken(TokenProvider tokenProvider, MemberRepository memberRepository, JdbcTemplate jdbcTemplate) {
        memberRepository.deleteAll();
        jdbcTemplate.update("alter table member auto_increment= ?", 1);

        Member member = new Member("abc@gmail.com", "1q2w3e4r!", "hong", null);
        memberRepository.save(member);
        return tokenProvider.createAccessToken(1L, Role.USER);
    }
}
