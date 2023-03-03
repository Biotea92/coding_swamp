package com.study.codingswamp.presentation.member;

import com.study.codingswamp.presentation.common.AuthenticatedMember;
import com.study.codingswamp.presentation.common.Login;
import com.study.codingswamp.application.auth.MemberPayload;
import com.study.codingswamp.domain.member.dto.request.MemberEditRequest;
import com.study.codingswamp.domain.member.dto.response.MemberResponse;
import com.study.codingswamp.domain.member.service.MemberService;
import com.study.codingswamp.domain.member.dto.request.MemberSignupRequest;
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
        MemberResponse response = memberService.edit(memberPayload.getId(), memberEditRequest);
        return ResponseEntity
                .created(URI.create("/api/member/" + memberPayload.getId()))
                .body(response);
    }

    @Login
    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticatedMember MemberPayload memberPayload) {
        memberService.delete(memberPayload.getId());
        return ResponseEntity.noContent().build();
    }
}
