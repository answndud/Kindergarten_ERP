package com.erp.common;

import com.erp.domain.classroom.repository.ClassroomRepository;
import com.erp.domain.kindergarten.repository.KindergartenRepository;
import com.erp.domain.kid.repository.KidRepository;
import com.erp.domain.kid.repository.ParentKidRepository;
import com.erp.domain.member.repository.MemberRepository;
import com.erp.domain.attendance.repository.AttendanceRepository;
import com.erp.domain.notepad.repository.NotepadRepository;
import com.erp.domain.announcement.repository.AnnouncementRepository;
import jakarta.persistence.EntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 테스트용 설정 클래스
 */
@TestConfiguration
public class TestConfig {

    @Bean
    public TestData testData(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
                         KindergartenRepository kindergartenRepository, ClassroomRepository classroomRepository,
                         KidRepository kidRepository, ParentKidRepository parentKidRepository,
                         AttendanceRepository attendanceRepository, NotepadRepository notepadRepository,
                         AnnouncementRepository announcementRepository, EntityManager entityManager) {
        return new TestData(memberRepository, passwordEncoder, kindergartenRepository, classroomRepository,
                kidRepository, parentKidRepository, attendanceRepository, notepadRepository, announcementRepository, entityManager);
    }
}
