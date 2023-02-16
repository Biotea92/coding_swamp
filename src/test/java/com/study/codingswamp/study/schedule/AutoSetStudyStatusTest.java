package com.study.codingswamp.study.schedule;

import com.study.codingswamp.study.domain.Study;
import com.study.codingswamp.study.domain.StudyStatus;
import com.study.codingswamp.study.domain.StudyType;
import com.study.codingswamp.study.domain.Tag;
import com.study.codingswamp.study.domain.repository.StudyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class AutoSetStudyStatusTest {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private AutoSetStudyStatus autoSetStudyStatus;

    @Test
    @DisplayName("메서드 호출시 StudyStatus가 업데이트 된다.")
    void scheduleTaskUsingCron() {
        // given
        List<Study> studies = IntStream.range(0, 10)
                .mapToObj(i -> Study.builder()
                        .title("제목입니다. " + i)
                        .description("설명입니다. " + i)
                        .studyStatus(StudyStatus.PREPARING)
                        .studyType(StudyType.STUDY)
                        .startDate(LocalDate.of(2023, 2, 1))
                        .endDate(LocalDate.of(2023, 2, 3))
                        .currentMemberCount(1)
                        .maxMemberCount(30)
                        .thumbnail("#00000")
                        .applicants(new HashSet<>())
                        .participants(new HashSet<>())
                        .tags(List.of(new Tag("태그1"), new Tag("태그2")))
                        .build()
                )
                .collect(Collectors.toList());
        studyRepository.saveAll(studies);

        // when
        autoSetStudyStatus.scheduleTaskUsingCron();

        // then
        assertEquals(StudyStatus.COMPLETION ,studies.get(0).getStudyStatus());
        assertEquals(StudyStatus.COMPLETION ,studies.get(9).getStudyStatus());
    }
}