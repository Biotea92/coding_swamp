package com.study.codingswamp.study.service;

import com.study.codingswamp.auth.service.MemberPayload;
import com.study.codingswamp.common.exception.NotFoundException;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import com.study.codingswamp.study.domain.Applicant;
import com.study.codingswamp.study.domain.Participant;
import com.study.codingswamp.study.domain.Study;
import com.study.codingswamp.study.domain.Tag;
import com.study.codingswamp.study.domain.repository.StudyRepository;
import com.study.codingswamp.study.service.request.ApplyRequest;
import com.study.codingswamp.study.service.request.StudiesPageableRequest;
import com.study.codingswamp.study.service.request.StudyRequest;
import com.study.codingswamp.study.service.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final MemberRepository memberRepository;

    public Study createStudy(MemberPayload memberPayload, StudyRequest request) {
        Member owner = findMember(memberPayload.getId());
        Study study = request.mapToStudy(owner);
        return studyRepository.save(study);
    }

    public StudyDetailResponse getStudyDetails(Long studyId) {
        Study study = findStudy(studyId);
        Member owner = study.getOwner();
        return StudyDetailResponse.builder()
                .study(study)
                .owner(new OwnerResponse(study, owner))
                .participants(getParticipationResponses(study))
                .applicants(getApplicantResponse(study))
                .tags(getTags(study.getTags()))
                .build();
    }

    @Transactional
    public void apply(MemberPayload memberPayload, Long studyId, ApplyRequest applyRequest) {
        Member applicantMember = findMember(memberPayload.getId());
        Study findStudy = findStudy(studyId);

        findStudy.validateStudyMaxMember();
        findStudy.checkParticipant(applicantMember);
        findStudy.checkApplicant(applicantMember);

        findStudy.addApplicant(new Applicant(applicantMember, applyRequest.getReasonForApplication(), LocalDate.now()));
    }

    @Transactional
    public void approve(MemberPayload memberPayload, Long studyId, Long applicantId) {
        Member owner = findMember(memberPayload.getId());
        Study findStudy = findStudy(studyId);
        findStudy.validateOwner(owner);
        findStudy.validateStudyMaxMember();
        Member applicantMember = findMember(applicantId);
        findStudy.checkParticipant(applicantMember);
        findStudy.addParticipant(new Participant(applicantMember, LocalDate.now()));
    }

    public StudiesResponse getStudies(StudiesPageableRequest request) {
        List<StudyResponse> studyResponses = studyRepository.getStudies(request)
                .stream()
                .map(study -> new StudyResponse(study, getTags(study.getTags())))
                .collect(Collectors.toList());
        Long totalCount = studyRepository.getCount();

        return new StudiesResponse(studyResponses, request.getTotalPage(totalCount));
    }

    public StudiesResponse getMyApplies(MemberPayload memberPayload) {
        Member member = findMember(memberPayload.getId());
        List<StudyResponse> studyResponses = studyRepository.findMyApplies(member)
                .stream()
                .map(study -> new StudyResponse(study, getTags(study.getTags())))
                .collect(Collectors.toList());
        return new StudiesResponse(studyResponses, 1);
    }

    public StudiesResponse getMyParticipates(MemberPayload memberPayload) {
        Member member = findMember(memberPayload.getId());
        List<StudyResponse> studyResponses = studyRepository.findMyParticipates(member)
                .stream()
                .map(study -> new StudyResponse(study, getTags(study.getTags())))
                .collect(Collectors.toList());
        return new StudiesResponse(studyResponses, 1);
    }

    @Transactional
    public Study edit(MemberPayload memberPayload, Long studyId, StudyRequest request) {
        Study findStudy = findStudy(studyId);
        Member owner = findMember(memberPayload.getId());
        findStudy.validateOwner(owner);
        findStudy.update(request);
        return findStudy;
    }

    private Study findStudy(Long studyId) {
        return studyRepository.findById(studyId)
                .orElseThrow(() -> new NotFoundException("studyId", "스터디를 찾을 수 없습니다."));
    }

    private List<ParticipantResponse> getParticipationResponses(Study study) {
        return study.getParticipants()
                .stream()
                .map(participant -> new ParticipantResponse(participant.getMember(), participant.getParticipationDate()))
                .collect(Collectors.toList());
    }

    private List<ApplicantResponse> getApplicantResponse(Study study) {
        return study.getApplicants()
                .stream()
                .map(applicant -> new ApplicantResponse(applicant.getMember(), applicant.getReasonForApplication(), applicant.getApplicantDate()))
                .collect(Collectors.toList());
    }

    private List<String> getTags(List<Tag> tags) {
        return tags.stream()
                .map(Tag::getTagText)
                .collect(Collectors.toList());
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("member", "사용자를 찾을 수 없습니다."));
    }
}
