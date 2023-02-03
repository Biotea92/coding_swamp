package com.study.codingswamp.study.controller;

import com.study.codingswamp.auth.config.AuthenticatedMember;
import com.study.codingswamp.auth.config.Login;
import com.study.codingswamp.auth.service.MemberPayload;
import com.study.codingswamp.study.domain.Study;
import com.study.codingswamp.study.service.StudyService;
import com.study.codingswamp.study.service.request.StudyCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/study")
public class StudyController {

    private final StudyService studyService;

    @Login
    @PostMapping
    public ResponseEntity<Void> create(@AuthenticatedMember MemberPayload memberPayload, @Validated @RequestBody StudyCreateRequest request) {
        Study study = studyService.createStudy(memberPayload, request);
        return ResponseEntity.created(URI.create("/api/study/" + study.getId())).build();
    }

    @GetMapping("/{studyId}")
    public ResponseEntity<Long> getStudyDetails(@PathVariable Long studyId) {
        return ResponseEntity.ok().body(studyId);
    }
}
