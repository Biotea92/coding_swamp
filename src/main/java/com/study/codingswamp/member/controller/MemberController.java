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

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberResponse> signup(@Validated MemberSignupRequest memberSignupRequest) {
        MemberResponse response = memberService.signup(memberSignupRequest);
        return ResponseEntity
                .created(URI.create("/api/member/" + response.getMemberId()))
                .body(response);
    }

    @GetMapping("/{memberId}")
    public MemberResponse getMember(@PathVariable Long memberId) {
        return memberService.getMember(memberId);
    }

    @Login
    @PostMapping("/edit")
    public ResponseEntity<MemberResponse> edit(@AuthenticatedMember MemberPayload memberPayload,
                                               @Validated MemberEditRequest memberEditRequest) {
        MemberResponse response = memberService.edit(memberPayload, memberEditRequest);
        return ResponseEntity
                .created(URI.create("/api/member/" + memberPayload.getId()))
                .body(response);
    }

    @Login
    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticatedMember MemberPayload memberPayload) {
        memberService.delete(memberPayload);
        return ResponseEntity.noContent().build();
    }
}
