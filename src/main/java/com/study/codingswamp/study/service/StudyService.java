package com.study.codingswamp.study.service;

import com.study.codingswamp.auth.service.MemberPayload;
import com.study.codingswamp.common.exception.ConflictException;
import com.study.codingswamp.common.exception.NotFoundException;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import com.study.codingswamp.study.domain.Applicant;
import com.study.codingswamp.study.domain.Participant;
import com.study.codingswamp.study.domain.Study;
import com.study.codingswamp.study.domain.Tag;
import com.study.codingswamp.study.domain.repository.StudyRepository;
import com.study.codingswamp.study.service.request.ApplyRequest;
import com.study.codingswamp.study.service.request.StudyCreateRequest;
import com.study.codingswamp.study.service.response.ApplicantResponse;
import com.study.codingswamp.study.service.response.OwnerResponse;
import com.study.codingswamp.study.service.response.ParticipantResponse;
import com.study.codingswamp.study.service.response.StudyDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final MemberRepository memberRepository;

    public Study createStudy(MemberPayload memberPayload, StudyCreateRequest request) {
        Member owner = findMember(memberPayload.getId(), "사용자를 찾을 수 없습니다.");
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
        Member applicantMember = findMember(memberPayload.getId(), "신청자를 찾을 수 없습니다.");
        Study findStudy = findStudy(studyId);

        validateStudyMaxMember(findStudy);
        checkParticipant(applicantMember, findStudy);
        checkApplicant(applicantMember, findStudy);

        findStudy.addApplicant(new Applicant(applicantMember, applyRequest.getReasonForApplication(), LocalDate.now()));
    }

    private void checkApplicant(Member member, Study findStudy) {
        Optional<Applicant> findApplicant = findStudy.getApplicants().stream()
                .filter(applicant -> applicant.getMember().getId().equals(member.getId()))
                .findAny();
        if (findApplicant.isPresent()) {
            throw new ConflictException("applicant", "이미 신청한 사용자입니다.");
        }
    }

    private void checkParticipant(Member member, Study findStudy) {
        Optional<Participant> findParticipant = findStudy.getParticipants().stream()
                .filter(participant -> participant.getMember() == member)
                .findAny();
        if (findParticipant.isPresent()) {
            throw new ConflictException("participant", "이미 참가한 인원입니다.");
        }
    }

    private void validateStudyMaxMember(Study study) {
        if (study.getCurrentMemberCount() == study.getMaxMemberCount()) {
            throw new ConflictException("study", "최대 정원인 스터디입니다.");
        }
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

    private Member findMember(Long memberId, String errorMessage) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("member", errorMessage));
    }
}
