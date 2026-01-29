package com.erp.common;

import com.erp.domain.classroom.entity.Classroom;
import com.erp.domain.classroom.repository.ClassroomRepository;
import com.erp.domain.kid.entity.Gender;
import com.erp.domain.kid.entity.Kid;
import com.erp.domain.kid.repository.KidRepository;
import com.erp.domain.kid.repository.ParentKidRepository;
import com.erp.domain.attendance.repository.AttendanceRepository;
import com.erp.domain.notepad.repository.NotepadRepository;
import com.erp.domain.announcement.repository.AnnouncementRepository;
import jakarta.persistence.EntityManager;
import com.erp.domain.kindergarten.entity.Kindergarten;
import com.erp.domain.kindergarten.repository.KindergartenRepository;
import com.erp.domain.member.entity.Member;
import com.erp.domain.member.entity.MemberRole;
import com.erp.domain.member.entity.MemberStatus;
import com.erp.domain.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 테스트용 데이터 헬퍼 클래스
 */
public class TestData {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final KindergartenRepository kindergartenRepository;
    private final ClassroomRepository classroomRepository;
    private final KidRepository kidRepository;
    private final ParentKidRepository parentKidRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotepadRepository notepadRepository;
    private final AnnouncementRepository announcementRepository;
    private final EntityManager entityManager;

    public TestData(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
                  KindergartenRepository kindergartenRepository, ClassroomRepository classroomRepository,
                  KidRepository kidRepository, ParentKidRepository parentKidRepository,
                  AttendanceRepository attendanceRepository, NotepadRepository notepadRepository,
                  AnnouncementRepository announcementRepository, EntityManager entityManager) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.kindergartenRepository = kindergartenRepository;
        this.classroomRepository = classroomRepository;
        this.kidRepository = kidRepository;
        this.parentKidRepository = parentKidRepository;
        this.attendanceRepository = attendanceRepository;
        this.notepadRepository = notepadRepository;
        this.announcementRepository = announcementRepository;
        this.entityManager = entityManager;
    }

    /**
     * 테스트용 회원 생성
     */
    public Member createTestMember(String email, String name, MemberRole role, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        Member member = Member.create(email, encodedPassword, name, "010-0000-0000", role);
        member.activate(); // 활성 상태로 변경
        return memberRepository.save(member);
    }

    /**
     * 테스트용 원장 회원 생성
     */
    public Member createPrincipalMember() {
        return createTestMember("principal@test.com", "원장님", MemberRole.PRINCIPAL, "test1234");
    }

    /**
     * 테스트용 교사 회원 생성
     */
    public Member createTeacherMember() {
        return createTestMember("teacher@test.com", "김선생", MemberRole.TEACHER, "test1234");
    }

    /**
     * 테스트용 학부모 회원 생성
     */
    public Member createParentMember() {
        return createTestMember("parent@test.com", "학부모", MemberRole.PARENT, "test1234");
    }

    /**
     * 테스트용 유치원 생성
     */
    public Kindergarten createKindergarten() {
        Kindergarten kindergarten = Kindergarten.create("테스트 유치원", "서울시", "010-0000-0000",
                LocalTime.of(9, 0), LocalTime.of(18, 0));
        return kindergartenRepository.save(kindergarten);
    }

    /**
     * 테스트용 반 생성
     */
    public Classroom createClassroom(Kindergarten kindergarten) {
        Classroom classroom = Classroom.create(kindergarten, "테스트반", "5세");
        return classroomRepository.save(classroom);
    }

    /**
     * 테스트용 원생 생성
     */
    public Kid createKid(Classroom classroom) {
        Kid kid = Kid.create(classroom, "테스트 원생", LocalDate.of(2020, 1, 1),
                Gender.MALE, LocalDate.now());
        return kidRepository.save(kid);
    }

    /**
     * 모든 테스트 데이터 정리
     */
    public void cleanup() {
        entityManager.flush();
        entityManager.clear();

        entityManager.createNativeQuery("DELETE FROM NOTEPAD_READ_CONFIRM").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM NOTIFICATION").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM ATTENDANCE").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM NOTEPAD").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM ANNOUNCEMENT").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM PARENT_KID").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM KID").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM CLASSROOM").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM MEMBER").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM KINDERGARTEN").executeUpdate();
    }
}
