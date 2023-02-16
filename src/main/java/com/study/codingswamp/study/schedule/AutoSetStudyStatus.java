package com.study.codingswamp.study.schedule;

import com.study.codingswamp.study.domain.Study;
import com.study.codingswamp.study.domain.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutoSetStudyStatus {

    private final StudyRepository studyRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void scheduleTaskUsingCron() {
        LocalDate now = LocalDate.now();
        log.info("Time to Start scheduled cron={}", now);
        List<Study> studyList = studyRepository.findStudyStatusIsNotCompleted();
        log.info("Checking StudyList Size={}", studyList.size());
        studyList.forEach(study -> study.updateStudyStatus(now));
    }
}
