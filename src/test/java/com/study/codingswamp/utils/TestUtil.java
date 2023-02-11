package com.study.codingswamp.utils;

import com.study.codingswamp.auth.token.TokenProvider;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.repository.MemberRepository;


public class TestUtil {

    public String saveMemberAndGetToken(TokenProvider tokenProvider, MemberRepository memberRepository) {
        Member member = new Member("abc@gmail.com", "1q2w3e4r!", "hong", "https://firebasestorage.googleapis.com/v0/b/coding-swamp.appspot.com/o/default_image%2Fcrocodile.png?alt=media");
        memberRepository.save(member);
        return tokenProvider.createAccessToken(member.getId(), member.getRole());
    }
}
