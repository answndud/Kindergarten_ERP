package com.erp.global.config;

import com.erp.domain.announcement.entity.Announcement;
import com.erp.domain.announcement.repository.AnnouncementRepository;
import com.erp.domain.attendance.entity.Attendance;
import com.erp.domain.attendance.entity.AttendanceStatus;
import com.erp.domain.attendance.repository.AttendanceRepository;
import com.erp.domain.classroom.entity.Classroom;
import com.erp.domain.classroom.repository.ClassroomRepository;
import com.erp.domain.kindergarten.entity.Kindergarten;
import com.erp.domain.kindergarten.repository.KindergartenRepository;
import com.erp.domain.kid.entity.Gender;
import com.erp.domain.kid.entity.Kid;
import com.erp.domain.kid.entity.ParentKid;
import com.erp.domain.kid.entity.Relationship;
import com.erp.domain.kid.repository.KidRepository;
import com.erp.domain.kid.repository.ParentKidRepository;
import com.erp.domain.member.entity.Member;
import com.erp.domain.member.entity.MemberRole;
import com.erp.domain.member.entity.MemberStatus;
import com.erp.domain.member.repository.MemberRepository;
import com.erp.domain.notepad.entity.Notepad;
import com.erp.domain.notepad.repository.NotepadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 로컬 개발용 더미 데이터 로더
 *
 * 테스트 계정 정보 (비밀번호: test1234!):
 * - 원장A: principal@test.com / test1234!
 * - 원장B: principal2@test.com / test1234!
 * - 선생A1: teacher1@test.com / test1234!
 * - 선생A2: teacher2@test.com / test1234!
 * - 선생B1: teacher3@test.com / test1234!
 * - 선생B2: teacher4@test.com / test1234!
 * - 학부모A1-3: parent{1,2,3}@test.com / test1234!
 * - 학부모B1-3: parent{4,5,6}@test.com / test1234!
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private static final String SEED_PRINCIPAL_A_EMAIL = "principal@test.com";
    private static final String SEED_PRINCIPAL_B_EMAIL = "principal2@test.com";

    private final PasswordEncoder passwordEncoder;
    private final KindergartenRepository kindergartenRepository;
    private final MemberRepository memberRepository;
    private final ClassroomRepository classroomRepository;
    private final KidRepository kidRepository;
    private final ParentKidRepository parentKidRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotepadRepository notepadRepository;
    private final AnnouncementRepository announcementRepository;
    private final Random random = new Random();

    // 테스트용 고정 비밀번호
    private static final String TEST_PASSWORD = "test1234!";

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 시드 계정이 이미 있으면 중복 생성 방지
        if (memberRepository.existsByEmail(SEED_PRINCIPAL_A_EMAIL)
                || memberRepository.existsByEmail(SEED_PRINCIPAL_B_EMAIL)) {
            log.info("Seed principals already exist. Skipping data loading.");
            log.info("Test password: {}", TEST_PASSWORD);
            return;
        }

        log.info("Loading dummy data...");

        // 1. 유치원 2개 생성
        Kindergarten kgA = createKindergarten("해바라기 유치원", "서울시 강남구 테헤란로 123", "02-1234-5678");
        Kindergarten kgB = createKindergarten("꿈나무 유치원", "서울시 서초구 강남대로 456", "02-9876-5432");

        // 2. 원장 2명 생성 (각 유치원 1명씩)
        Member principalA = createMember("principal@test.com", "김원장", MemberRole.PRINCIPAL, kgA);
        Member principalB = createMember("principal2@test.com", "이원장", MemberRole.PRINCIPAL, kgB);

        // 3. 선생님 4명 생성 (A: 2명, B: 2명)
        Member teacherA1 = createMember("teacher1@test.com", "김교사", MemberRole.TEACHER, kgA);
        Member teacherA2 = createMember("teacher2@test.com", "박교사", MemberRole.TEACHER, kgA);
        Member teacherB1 = createMember("teacher3@test.com", "최교사", MemberRole.TEACHER, kgB);
        Member teacherB2 = createMember("teacher4@test.com", "정교사", MemberRole.TEACHER, kgB);

        // 4. 반 4개 생성 (각 유치원 2개씩)
        Classroom classA1 = createClassroom(kgA, "해바라기반", "5세", teacherA1);
        Classroom classA2 = createClassroom(kgA, "장미반", "6세", teacherA2);
        Classroom classB1 = createClassroom(kgB, "나무반", "5세", teacherB1);
        Classroom classB2 = createClassroom(kgB, "꽃반", "6세", teacherB2);

        // 5. 원아 생성 (A: 6명, B: 6명 = 총 12명)
        List<Kid> kidsA = new ArrayList<>();
        kidsA.addAll(createKidsForClassroom(classA1, "준우", "서윤", "도윤"));
        kidsA.addAll(createKidsForClassroom(classA2, "시우", "하은", "지호"));

        List<Kid> kidsB = new ArrayList<>();
        kidsB.addAll(createKidsForClassroom(classB1, "주원", "수빈", "지원"));
        kidsB.addAll(createKidsForClassroom(classB2, "다은", "예준", "연우"));

        // 6. 학부모 생성 (A: 3명, B: 3명 = 총 6명)
        // A 유치원 학부모 3명 (각각 여러 자녀 연결)
        Member parentA1 = createMember("parent1@test.com", "준우아빠", MemberRole.PARENT, kgA);
        Member parentA2 = createMember("parent2@test.com", "서윤엄마", MemberRole.PARENT, kgA);
        Member parentA3 = createMember("parent3@test.com", "시우할아빠", MemberRole.PARENT, kgA);

        // A 유치원 자녀들과 부모 연결
        createParentKid(parentA1, kidsA.get(0), Relationship.FATHER);  // 준우
        createParentKid(parentA1, kidsA.get(3), Relationship.FATHER);  // 시우
        createParentKid(parentA2, kidsA.get(1), Relationship.MOTHER);  // 서윤
        createParentKid(parentA2, kidsA.get(4), Relationship.MOTHER);  // 하은
        createParentKid(parentA3, kidsA.get(2), Relationship.FATHER);  // 도윤
        createParentKid(parentA3, kidsA.get(5), Relationship.FATHER);  // 지호

        // B 유치원 학부모 3명
        Member parentB1 = createMember("parent4@test.com", "주원엄마", MemberRole.PARENT, kgB);
        Member parentB2 = createMember("parent5@test.com", "수빈아빠", MemberRole.PARENT, kgB);
        Member parentB3 = createMember("parent6@test.com", "지원할머니", MemberRole.PARENT, kgB);

        // B 유치원 자녀들과 부모 연결
        createParentKid(parentB1, kidsB.get(0), Relationship.MOTHER);  // 주원
        createParentKid(parentB1, kidsB.get(3), Relationship.MOTHER);  // 다은
        createParentKid(parentB2, kidsB.get(1), Relationship.FATHER);  // 수빈
        createParentKid(parentB2, kidsB.get(4), Relationship.FATHER);  // 예준
        createParentKid(parentB3, kidsB.get(2), Relationship.GRANDMOTHER);  // 지원
        createParentKid(parentB3, kidsB.get(5), Relationship.GRANDMOTHER);  // 연우

        // 7. 출석부 생성 (최근 7일간)
        LocalDate today = LocalDate.now();
        for (Kid kid : kidsA) {
            for (int i = 0; i < 7; i++) {
                LocalDate date = today.minusDays(i);
                createAttendance(kid, date);
            }
        }
        for (Kid kid : kidsB) {
            for (int i = 0; i < 7; i++) {
                LocalDate date = today.minusDays(i);
                createAttendance(kid, date);
            }
        }

        // 8. 알림장 생성
        createNotepad(classA1, teacherA1, "오늘의 활동 안내", "오늘은 미술 시간에 그림 그리기 활동을 했습니다. 아이들이 참 재미있어하네요!", null);
        createNotepad(classA1, teacherA1, "주간 식단 안내", "이번 주 월요일: 김밥, 화요일: 비빔밥, 수요목: 돈가스, 금요일: 떡국", null);
        createNotepad(classA2, teacherA2, "현장 학습 안내", "다음 주 화요일은 과학관으로 현장 학습을 갑니다. 간편한 복장으로 와주세요.", null);
        createNotepad(classA2, teacherA2, "날씨에 따른 준비물", "내일은 비가 온다고 하니 우산을 꼭 챙겨주세요.", null);
        createNotepad(classB1, teacherB1, "체육 대회 연습", "이번 주부터 체육 대회 연습을 시작합니다. 운동화를 꼭 신어주세요.", null);
        createNotepad(classB1, teacherB1, "도서관 이용 안내", "매주 수요일은 도서관 날입니다. 도서 대출증을 챙겨주세요.", null);
        createNotepad(classB2, teacherB2, "음악 발표회", "이번 달 말에 음악 발표회가 있습니다. 악기 연습을 열심히 해주세요.", null);
        createNotepad(classB2, teacherB2, "비 오는 날 실내 놀이", "비가 오는 날은 실내에서 보드 게임과 블록 놀이를 합니다.", null);

        // 원아별 알림장
        createNotepad(classA1, teacherA1, "준우 생일 축하", "오늘 준우의 5번째 생일을 축하합니다!", kidsA.get(0));
        createNotepad(classA2, teacherA2, "시우 칭찬 일기", "시우가 친구들과 사이좋게 지내는 모습이 아주 좋습니다.", kidsA.get(3));

        // 9. 공지사항 생성
        createAnnouncement(kgA, principalA, "[긴급] 송파구 코로나19 확진자 동선 안내", "송파구에 코로나19 확진자 동선이 발생하여 이를 안내드립니다.", true);
        createAnnouncement(kgA, principalA, "5월 가정 통신문 발송 안내", "5월 가정 통신문을 오늘 발송하였습니다. 확인 부탁드립니다.", false);
        createAnnouncement(kgA, principalA, "여름 방학 일정 안내", "올해 여름 방학은 7월 20일부터 8월 20일까지입니다.", false);
        createAnnouncement(kgB, principalB, "[중요] 어린이잔치 행사 안내", "다음 주 5일 어린이날을 맞아 특별 행사가 준비되었습니다.", true);
        createAnnouncement(kgB, principalB, "새 학기 입학 안내", "2025학년도 새 학기 입학 원서 접수가 시작되었습니다.", false);
        createAnnouncement(kgB, principalB, "급식비 납부 안내", "이번 달 급식비를 5월 10일까지 납부부탁드립니다.", false);

        log.info("=================================================");
        log.info("DUMMY DATA LOADED SUCCESSFULLY!");
        log.info("=================================================");
        log.info("TEST PASSWORD: {}", TEST_PASSWORD);
        log.info("---------------------------------------------------");
        log.info("유치원 A (해바라기 유치원):");
        log.info("  원장:   principal@test.com / {}", TEST_PASSWORD);
        log.info("  선생님: teacher1@test.com, teacher2@test.com");
        log.info("  학부모: parent1@test.com (준우,시우아빠)");
        log.info("          parent2@test.com (서윤,하은엄마)");
        log.info("          parent3@test.com (도윤,지호할아빠)");
        log.info("---------------------------------------------------");
        log.info("유치원 B (꿈나무 유치원):");
        log.info("  원장:   principal2@test.com / {}", TEST_PASSWORD);
        log.info("  선생님: teacher3@test.com, teacher4@test.com");
        log.info("  학부모: parent4@test.com (주원,다은엄마)");
        log.info("          parent5@test.com (수빈,예준아빠)");
        log.info("          parent6@test.com (지원,연우할머니)");
        log.info("=================================================");
        log.info("총 생성: 2 유치원, 2 원장, 4 선생님, 6 학부모, 4 반, 12 원아");
        log.info("=================================================");
    }

    private Kindergarten createKindergarten(String name, String address, String phone) {
        Kindergarten kg = Kindergarten.create(name, address, phone, LocalTime.of(9, 0), LocalTime.of(18, 0));
        return kindergartenRepository.save(kg);
    }

    private Member createMember(String email, String name, MemberRole role, Kindergarten kindergarten) {
        Member member = Member.create(email, passwordEncoder.encode(TEST_PASSWORD), name, "010-1234-5678", role);
        member.assignKindergarten(kindergarten);
        return memberRepository.save(member);
    }

    private Classroom createClassroom(Kindergarten kg, String name, String ageGroup, Member teacher) {
        Classroom classroom = Classroom.create(kg, name, ageGroup);
        classroom.assignTeacher(teacher);
        return classroomRepository.save(classroom);
    }

    private List<Kid> createKidsForClassroom(Classroom classroom, String... names) {
        List<Kid> kids = new ArrayList<>();
        int birthYear = LocalDate.now().getYear() - 5;
        for (String name : names) {
            Kid kid = Kid.create(
                    classroom,
                    name,
                    LocalDate.of(birthYear, 3, 15),
                    random.nextBoolean() ? Gender.MALE : Gender.FEMALE,
                    LocalDate.of(LocalDate.now().getYear(), 3, 1)
            );
            kids.add(kidRepository.save(kid));
        }
        return kids;
    }

    private ParentKid createParentKid(Member parent, Kid kid, Relationship relationship) {
        ParentKid parentKid = ParentKid.create(kid, parent, relationship);
        return parentKidRepository.save(parentKid);
    }

    private void createAttendance(Kid kid, LocalDate date) {
        AttendanceStatus status = determineAttendanceStatus(date);

        Attendance attendance = Attendance.create(kid, date, status);

        if (status == AttendanceStatus.PRESENT) {
            attendance.recordDropOff(LocalTime.of(9, random.nextInt(30)));
            attendance.recordPickUp(LocalTime.of(16, random.nextInt(60)));
        } else {
            attendance.updateAttendance(status, "자택 연락");
        }

        attendanceRepository.save(attendance);
    }

    private AttendanceStatus determineAttendanceStatus(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int dayValue = dayOfWeek.getValue();
        int statusIndex = dayValue % 5;

        return switch (statusIndex) {
            case 0 -> AttendanceStatus.ABSENT;
            case 1 -> AttendanceStatus.LATE;
            case 2 -> AttendanceStatus.EARLY_LEAVE;
            case 3 -> AttendanceStatus.SICK_LEAVE;
            default -> AttendanceStatus.PRESENT;
        };
    }

    private void createNotepad(Classroom classroom, Member writer, String title, String content, Kid kid) {
        Notepad notepad;
        if (kid != null) {
            notepad = Notepad.createKidNotepad(kid, writer, title, content);
        } else {
            notepad = Notepad.createClassroomNotepad(classroom, writer, title, content);
        }
        notepadRepository.save(notepad);
    }

    private void createAnnouncement(Kindergarten kg, Member writer, String title, String content, boolean important) {
        Announcement announcement;
        if (important) {
            announcement = Announcement.createImportant(kg, writer, title, content);
        } else {
            announcement = Announcement.create(kg, writer, title, content);
        }
        announcementRepository.save(announcement);
    }
}
