package com.study.codingswamp.domain.study.service;

import com.study.codingswamp.domain.member.entity.Member;
import com.study.codingswamp.domain.member.repository.MemberRepository;
import com.study.codingswamp.domain.study.dto.request.ApplyRequest;
import com.study.codingswamp.domain.study.dto.request.SearchCondition;
import com.study.codingswamp.domain.study.dto.request.StudiesPageableRequest;
import com.study.codingswamp.domain.study.dto.request.StudyRequest;
import com.study.codingswamp.domain.study.dto.response.*;
import com.study.codingswamp.domain.study.entity.*;
import com.study.codingswamp.domain.study.repository.ApplicantRepository;
import com.study.codingswamp.domain.study.repository.ParticipantRepository;
import com.study.codingswamp.domain.study.repository.ReviewRepository;
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
    private final ReviewRepository reviewRepository;

    @Transactional
    public Study createStudy(Long memberId, StudyRequest request) {
        Member owner = findMember(memberId);
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
    public void apply(Long memberId, Long studyId, ApplyRequest applyRequest) {
        Member applicantMember = findMember(memberId);
        Study findStudy = findStudy(studyId);

        findStudy.validateStudyMaxMember();
        findStudy.checkParticipant(applicantMember);
        findStudy.checkApplicant(applicantMember);

        Applicant applicant = new Applicant(findStudy, applicantMember, applyRequest.getReasonForApplication(), LocalDate.now());
        applicantRepository.save(applicant);
        findStudy.addApplicant(applicant);
    }

    @Transactional
    public void approve(Long memberId, Long studyId, Long applicantId) {
        Member owner = findMember(memberId);
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

    public StudiesResponse getMyApplies(Long memberId) {
        Member member = findMember(memberId);
        List<StudyResponse> studyResponses = studyRepository.findMyApplies(member)
                .stream()
                .map(study -> new StudyResponse(study, getTags(study.getTags())))
                .collect(Collectors.toList());
        return new StudiesResponse(studyResponses, 1);
    }

    public StudiesResponse getMyParticipates(Long memberId) {
        Member member = findMember(memberId);
        List<StudyResponse> studyResponses = studyRepository.findMyParticipates(member)
                .stream()
                .map(study -> new StudyResponse(study, getTags(study.getTags())))
                .collect(Collectors.toList());
        return new StudiesResponse(studyResponses, 1);
    }

    @Transactional
    public void edit(Long memberId, Long studyId, StudyRequest request) {
        Study findStudy = findStudy(studyId);
        Member owner = findMember(memberId);
        findStudy.validateOwner(owner);
        findStudy.update(request);
    }

    @Transactional
    public void delete(Long memberId, Long studyId) {
        Study findStudy = findStudy(studyId);
        Member owner = findMember(memberId);
        findStudy.validateOwner(owner);
        participantRepository.deleteAll(findStudy.getParticipants());
        applicantRepository.deleteAll(findStudy.getApplicants());
        reviewRepository.deleteByStudyId(studyId);
        studyRepository.delete(findStudy);
    }

    @Transactional
    public void withdraw(Long memberId, Long studyId) {
        Study findStudy = findStudy(studyId);
        Member member = findMember(memberId);
        Participant participant = findStudy.withDrawParticipant(member);
        participantRepository.delete(participant);
    }

    @Transactional
    public void kickParticipant(Long memberId, Long studyId, Long participantMemberId) {
        Study findStudy = findStudy(studyId);
        Member owner = findMember(memberId);
        findStudy.validateOwner(owner);

        Member participantMember = findMember(participantMemberId);
        Participant participant = findStudy.kickParticipant(participantMember);
        participantRepository.delete(participant);
    }

    @Transactional
    public void cancelApply(Long memberId, Long studyId) {
        Study findStudy = findStudy(studyId);
        Member applicantMember = findMember(memberId);

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
