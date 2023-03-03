package com.study.codingswamp.study.controller;


import com.study.codingswamp.auth.config.AuthenticatedMember;
import com.study.codingswamp.auth.config.Login;
import com.study.codingswamp.auth.service.MemberPayload;
import com.study.codingswamp.study.service.ReviewService;
import com.study.codingswamp.study.service.request.ReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study")
public class ReviewController {

    private final ReviewService reviewService;

    @Login
    @PostMapping("/{studyId}/review")
    public ResponseEntity<Void> register(
            @AuthenticatedMember MemberPayload memberPayload,
            @PathVariable Long studyId,
            @RequestBody ReviewRequest request) {
        reviewService.register(memberPayload.getId(), studyId, request);
        return ResponseEntity.created(URI.create("/api/study/" + studyId + "/review")).build();
    }
}
