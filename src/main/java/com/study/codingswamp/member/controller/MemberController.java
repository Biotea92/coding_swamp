package com.study.codingswamp.member.controller;

import com.study.codingswamp.auth.config.AuthenticatedMember;
import com.study.codingswamp.auth.config.Login;
import com.study.codingswamp.auth.service.MemberPayload;
import com.study.codingswamp.member.service.MemberService;
import com.study.codingswamp.member.service.request.MemberEditRequest;
import com.study.codingswamp.member.service.request.MemberSignupRequest;
import com.study.codingswamp.member.service.response.MemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public MemberResponse signup(@Validated MemberSignupRequest memberSignupRequest) {
        return memberService.signup(memberSignupRequest);
    }

    @GetMapping("/{memberId}")
    public MemberResponse getMember(@PathVariable Long memberId) {
        return memberService.getMember(memberId);
    }

    @Login
    @PostMapping("/edit")
    public MemberResponse edit(@AuthenticatedMember MemberPayload memberPayload,
                               @Validated MemberEditRequest memberEditRequest) {
        return memberService.edit(memberPayload, memberEditRequest);
    }

    @Login
    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticatedMember MemberPayload memberPayload) {
        memberService.delete(memberPayload);
        return ResponseEntity.noContent().build();
    }
}
