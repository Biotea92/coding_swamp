package com.study.codingswamp.study.service;

import com.study.codingswamp.auth.service.MemberPayload;
import com.study.codingswamp.common.exception.ConflictException;
import com.study.codingswamp.common.exception.NotFoundException;
import com.study.codingswamp.member.domain.Member;
import com.study.codingswamp.member.domain.repository.MemberRepository;
import com.study.codingswamp.study.domain.Applicant;
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
        Member member = findMember(memberPayload.getId(), "member", "사용자를 찾을 수 없습니다.");
        Long ownerId = member.getId();
        Study study = request.mapToStudy(ownerId);
        return studyRepository.save(study);
    }

    public StudyDetailResponse getStudyDetails(Long studyId) {
        Study study = findStudy(studyId);
        Long ownerId = study.getOwnerId();
        Member owner = findMember(ownerId, "owner", "스터디 주인을 찾을 수 없습니다.");
        return StudyDetailResponse.builder()
                .study(study)
                .owner(new OwnerResponse(study, owner))
                .participants(getParticipationResponses(study))
                .applicants(getApplicantResponse(study))
                .tags(getTags(study.getTags()))
                .build();
    }

    public void apply(MemberPayload memberPayload, Long studyId, ApplyRequest applyRequest) {
        Member member = findMember(memberPayload.getId(), "member", "신청자를 찾을 수 없습니다.");
        Study findStudy = findStudy(studyId);

        validateStudyMaxMember(findStudy);

        Optional<Applicant> findApplicant = findStudy.getApplicants().stream()
                .filter(applicant -> applicant.getMemberId().equals(member.getId()))
                .findAny();
        if (findApplicant.isPresent()) {
            throw new ConflictException("applicant", "이미 신청한 사용자입니다.");
        }

        findStudy.addApplicant(new Applicant(member.getId(), applyRequest.getReasonForApplication(), LocalDate.now()));
    }

    private static void validateStudyMaxMember(Study study) {
        if (study.getCurrentMemberCount() == study.getMaxMemberCount()) {
            throw new ConflictException("study", "최대 정원인 스터디입니다.");
        }
    }

    private Study findStudy(Long studyId) {
        return studyRepository.findById(studyId).orElseThrow(() -> new NotFoundException("studyId", "스터디를 찾을 수 없습니다."));
    }

    private List<ParticipantResponse> getParticipationResponses(Study study) {
        return study.getParticipants()
                .stream()
                .map(participant -> {
                    Member member = findMember(participant.getMemberId(), "participant", "참여자를 찾을 수 없습니다.");
                    return new ParticipantResponse(member, participant.getParticipationDate());
                }).collect(Collectors.toList());
    }

    private List<ApplicantResponse> getApplicantResponse(Study study) {
        return study.getApplicants()
                .stream()
                .map(applicant -> {
                    Member member = findMember(applicant.getMemberId(), "applicant", "신청자를 찾을 수 없습니다.");
                    return new ApplicantResponse(member, applicant.getReasonForApplication(), applicant.getApplicantDate());
                }).collect(Collectors.toList());
    }

    private List<String> getTags(List<Tag> tags) {
        return tags.stream()
                .map(Tag::getTagText)
                .collect(Collectors.toList());
    }

    private Member findMember(Long memberPayload, String errorFieldName, String errorMessage) {
        return memberRepository.findById(memberPayload)
                .orElseThrow(() -> new NotFoundException(errorFieldName, errorMessage));
    }
}
