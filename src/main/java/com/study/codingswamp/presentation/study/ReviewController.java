package com.study.codingswamp.presentation.study;


import com.study.codingswamp.application.auth.MemberPayload;
import com.study.codingswamp.domain.study.dto.request.CursorRequest;
import com.study.codingswamp.domain.study.dto.request.ReviewRequest;
import com.study.codingswamp.domain.study.dto.response.PageCursor;
import com.study.codingswamp.domain.study.dto.response.ReviewResponse;
import com.study.codingswamp.domain.study.service.ReviewService;
import com.study.codingswamp.presentation.common.AuthenticatedMember;
import com.study.codingswamp.presentation.common.Login;
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

    @Login
    @GetMapping("/{studyId}/review")
    public PageCursor<ReviewResponse> getReviews (
            @AuthenticatedMember MemberPayload memberPayload,
            @PathVariable Long studyId,
            CursorRequest request
            ) {
        return reviewService.getReviews(memberPayload.getId(), studyId, request);
    }
}
