package com.study.codingswamp.study.controller;

import com.study.codingswamp.auth.config.AuthenticatedMember;
import com.study.codingswamp.auth.config.Login;
import com.study.codingswamp.auth.service.MemberPayload;
import com.study.codingswamp.study.domain.Study;
import com.study.codingswamp.study.service.StudyService;
import com.study.codingswamp.study.service.request.ApplyRequest;
import com.study.codingswamp.study.service.request.SearchCondition;
import com.study.codingswamp.study.service.request.StudiesPageableRequest;
import com.study.codingswamp.study.service.request.StudyRequest;
import com.study.codingswamp.study.service.response.StudiesResponse;
import com.study.codingswamp.study.service.response.StudyDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study")
public class StudyController {

    private final StudyService studyService;

    @Login
    @PostMapping
    public ResponseEntity<Void> create(@AuthenticatedMember MemberPayload memberPayload,
                                       @Validated @RequestBody StudyRequest request) {
        Study study = studyService.createStudy(memberPayload, request);
        return ResponseEntity.created(URI.create("/api/study/" + study.getId())).build();
    }

    @GetMapping("/{studyId}")
    public ResponseEntity<StudyDetailResponse> getStudyDetails(@PathVariable Long studyId) {
        StudyDetailResponse response = studyService.getStudyDetails(studyId);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<StudiesResponse> getStudies(@ModelAttribute StudiesPageableRequest request) {
        StudiesResponse response = studyService.getStudies(request);
        return ResponseEntity.ok(response);
    }

    @Login
    @PatchMapping("/{studyId}/apply")
    public ResponseEntity<Void> apply(@AuthenticatedMember MemberPayload memberPayload,
                                      @PathVariable Long studyId,
                                      @Validated @RequestBody ApplyRequest applyRequest) {
        studyService.apply(memberPayload, studyId, applyRequest);
        return ResponseEntity.created(URI.create("/api/study/" + studyId)).build();
    }

    @Login
    @PatchMapping("/{studyId}/approve/{applicantId}")
    public ResponseEntity<Void> approve(@AuthenticatedMember MemberPayload memberPayload,
                                        @PathVariable Long studyId,
                                        @PathVariable Long applicantId) {
        studyService.approve(memberPayload, studyId, applicantId);
        return ResponseEntity.created(URI.create("/api/study/" + studyId)).build();
    }

    @Login
    @GetMapping("/my/applies")
    public ResponseEntity<StudiesResponse> getMyApplies(@AuthenticatedMember MemberPayload memberPayload) {
        StudiesResponse response = studyService.getMyApplies(memberPayload);
        return ResponseEntity.ok(response);
    }

    @Login
    @GetMapping("/my/participates")
    public ResponseEntity<StudiesResponse> getMyParticipates(@AuthenticatedMember MemberPayload memberPayload) {
        StudiesResponse response = studyService.getMyParticipates(memberPayload);
        return ResponseEntity.ok(response);
    }

    @Login
    @PutMapping("/{studyId}")
    public ResponseEntity<Void> edit(@AuthenticatedMember MemberPayload memberPayload,
                                     @PathVariable Long studyId,
                                     @Validated @RequestBody StudyRequest request) {
        studyService.edit(memberPayload, studyId, request);
        return ResponseEntity.created(URI.create("/api/study/" + studyId)).build();
    }

    @Login
    @DeleteMapping("/{studyId}")
    public ResponseEntity<Void> delete(@AuthenticatedMember MemberPayload memberPayload,
                                       @PathVariable Long studyId) {
        studyService.delete(memberPayload, studyId);
        return ResponseEntity.noContent().build();
    }

    @Login
    @PatchMapping("/{studyId}/withdraw")
    public ResponseEntity<Void> withdraw(@AuthenticatedMember MemberPayload memberPayload,
                                         @PathVariable Long studyId) {
        studyService.withdraw(memberPayload, studyId);
        return ResponseEntity.created(URI.create("/api/study/" + studyId)).build();
    }

    @Login
    @PatchMapping("/{studyId}/kick/{memberId}")
    public ResponseEntity<Void> kick(@AuthenticatedMember MemberPayload memberPayload,
                                     @PathVariable Long studyId,
                                     @PathVariable Long memberId) {
        studyService.kickParticipant(memberPayload, studyId, memberId);
        return ResponseEntity.created(URI.create("/api/study/" + studyId)).build();
    }

    @Login
    @PatchMapping("/{studyId}/apply-cancel")
    public ResponseEntity<Void> cancelApply(
            @AuthenticatedMember MemberPayload memberPayload,
            @PathVariable Long studyId) {
        studyService.cancelApply(memberPayload, studyId);
        return ResponseEntity.created(URI.create("/api/study/" + studyId)).build();
    }

    @GetMapping("/search")
    public ResponseEntity<StudiesResponse> getSearchStudies(@ModelAttribute SearchCondition condition) {
        StudiesResponse response = studyService.getSearchStudies(condition);
        return ResponseEntity.ok(response);
    }
}
