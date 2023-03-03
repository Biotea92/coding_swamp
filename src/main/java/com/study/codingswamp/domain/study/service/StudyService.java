package com.study.codingswamp.domain.study.service;

import com.study.codingswamp.application.auth.service.MemberPayload;
import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.member.repository.MemberRepository;
import com.study.codingswamp.domain.study.dto.request.ApplyRequest;
import com.study.codingswamp.domain.study.dto.request.SearchCondition;
import com.study.codingswamp.domain.study.dto.request.StudiesPageableRequest;
import com.study.codingswamp.domain.study.dto.request.StudyRequest;
import com.study.codingswamp.domain.study.dto.response.*;
import com.study.codingswamp.domain.study.entity.Applicant;
import com.study.codingswamp.domain.study.entity.Participant;
import com.study.codingswamp.domain.study.entity.Study;
import com.study.codingswamp.domain.study.entity.Tag;
import com.study.codingswamp.domain.study.repository.ApplicantRepository;
import com.study.codingswamp.domain.study.repository.ParticipantRepository;
import com.study.codingswamp.domain.study.repository.StudyRepository;
import com.study.codingswamp.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyService {

    private final StudyRepository studyRepository;
    private final MemberRepository memberRepository;
    private final ApplicantRepository applicantRepository;
    private final ParticipantRepository participantRepository;

    @Transactional
    public Study createStudy(MemberPayload memberPayload, StudyRequest request) {
        Member owner = findMember(memberPayload.getId());
        Study study = request.mapToStudy(owner);
        Participant participant = new Participant(study, owner, LocalDate.now());
        study.initParticipants(participant);
        participantRepository.save(participant);
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

        Applicant applicant = new Applicant(findStudy, applicantMember, applyRequest.getReasonForApplication(), LocalDate.now());
        applicantRepository.save(applicant);
        findStudy.addApplicant(applicant);
    }

    @Transactional
    public void approve(MemberPayload memberPayload, Long studyId, Long applicantId) {
        Member owner = findMember(memberPayload.getId());
        Study findStudy = findStudy(studyId);
        findStudy.validateOwner(owner);
        findStudy.validateStudyMaxMember();
        Member applicantMember = findMember(applicantId);
        findStudy.checkParticipant(applicantMember);

        Participant participant = new Participant(findStudy, applicantMember, LocalDate.now());
        Applicant applicant = findStudy.addParticipant(participant);
        participantRepository.save(participant);
        applicantRepository.delete(applicant);
    }

    public StudiesResponse getStudies(StudiesPageableRequest request) {
        List<StudyResponse> studyResponses = studyRepository.getStudies(request)
                .stream()
                .map(study -> new StudyResponse(study, getTags(study.getTags())))
                .collect(Collectors.toList());
        Long totalCount = studyRepository.getCount();

        return new StudiesResponse(studyResponses, request.getTotalPage(totalCount));
    }

    public StudiesResponse getSearchStudies(SearchCondition condition) {
        List<StudyResponse> studyResponses = studyRepository.getSearchStudies(condition)
                .stream()
                .map(study -> new StudyResponse(study, getTags(study.getTags())))
                .collect(Collectors.toList());
        Long totalCount = studyRepository.getCount(condition);
        return new StudiesResponse(studyResponses, condition.getTotalPage(totalCount));
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
    public void edit(MemberPayload memberPayload, Long studyId, StudyRequest request) {
        Study findStudy = findStudy(studyId);
        Member owner = findMember(memberPayload.getId());
        findStudy.validateOwner(owner);
        findStudy.update(request);
    }

    @Transactional
    public void delete(MemberPayload memberPayload, Long studyId) {
        Study findStudy = findStudy(studyId);
        Member owner = findMember(memberPayload.getId());
        findStudy.validateOwner(owner);
        participantRepository.deleteAll(findStudy.getParticipants());
        applicantRepository.deleteAll(findStudy.getApplicants());
        studyRepository.delete(findStudy);
    }

    @Transactional
    public void withdraw(MemberPayload memberPayload, Long studyId) {
        Study findStudy = findStudy(studyId);
        Member member = findMember(memberPayload.getId());
        Participant participant = findStudy.withDrawParticipant(member);
        participantRepository.delete(participant);
    }

    @Transactional
    public void kickParticipant(MemberPayload memberPayload, Long studyId, Long memberId) {
        Study findStudy = findStudy(studyId);
        Member owner = findMember(memberPayload.getId());
        findStudy.validateOwner(owner);

        Member participantMember = findMember(memberId);
        Participant participant = findStudy.kickParticipant(participantMember);
        participantRepository.delete(participant);
    }

    @Transactional
    public void cancelApply(MemberPayload memberPayload, Long studyId) {
        Study findStudy = findStudy(studyId);
        Member applicantMember = findMember(memberPayload.getId());

        Applicant applicant = findStudy.removeApplicant(applicantMember);
        applicantRepository.delete(applicant);
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
