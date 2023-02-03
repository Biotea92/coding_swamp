package com.study.codingswamp.study.service;

import com.study.codingswamp.auth.service.MemberPayload;
import com.study.codingswamp.common.exception.NotFoundException;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import com.study.codingswamp.study.domain.Study;
import com.study.codingswamp.study.domain.Tag;
import com.study.codingswamp.study.domain.repository.StudyRepository;
import com.study.codingswamp.study.service.request.StudyCreateRequest;
import com.study.codingswamp.study.service.response.ApplicantResponse;
import com.study.codingswamp.study.service.response.OwnerResponse;
import com.study.codingswamp.study.service.response.ParticipantResponse;
import com.study.codingswamp.study.service.response.StudyDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final MemberRepository memberRepository;

    public Study createStudy(MemberPayload memberPayload, StudyCreateRequest request) {
        Member member = memberRepository.findById(memberPayload.getId())
                .orElseThrow(() -> new NotFoundException("member", "사용자를 찾을 수 없습니다."));
        Long ownerId = member.getId();
        Study study = request.mapToStudy(ownerId);
        return studyRepository.save(study);
    }

    public StudyDetailResponse getStudyDetails(Long studyId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new NotFoundException("study", "스터디를 찾을 수 없습니다."));
        Long ownerId = study.getOwnerId();
        Member owner = memberRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("owner", "스터디 주인을 찾을 수 없습니다."));
        return StudyDetailResponse.builder()
                .study(study)
                .owner(new OwnerResponse(study, owner))
                .participants(getParticipationResponses(study))
                .applicants(getApplicantResponse(study))
                .tags(getTags(study.getTags()))
                .build();
    }

    private List<ParticipantResponse> getParticipationResponses(Study study) {
        return study.getParticipants()
                .stream()
                .map(participant -> {
                    Member member = memberRepository.findById(participant.getMemberId())
                            .orElseThrow(() -> new NotFoundException("participant", "참여자를 찾을 수 없습니다."));
                    LocalDate participationDate = participant.getParticipationDate();
                    return new ParticipantResponse(member, participationDate);
                }).collect(Collectors.toList());
    }

    private List<ApplicantResponse> getApplicantResponse(Study study) {
        return study.getApplicants()
                .stream()
                .map(applicant -> {
                    Member member = memberRepository.findById(applicant.getMemberId())
                            .orElseThrow(() -> new NotFoundException("applicant", "신청자를 찾을 수 없습니다."));
                    LocalDate participationDate = applicant.getApplicantDate();
                    return new ApplicantResponse(member, participationDate);
                }).collect(Collectors.toList());
    }

    private List<String> getTags(List<Tag> tags) {
        return tags.stream()
                .map(Tag::getTagText)
                .collect(Collectors.toList());
    }
}
