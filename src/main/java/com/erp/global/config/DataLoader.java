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

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

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

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (kindergartenRepository.count() > 0) {
            log.info("Dummy data already exists. Skipping data loading.");
            return;
        }

        log.info("Loading dummy data...");

        // 1. 유치원 2개 생성
        Kindergarten kg1 = createKindergarten("해바라기 유치원", "서울시 강남구 테헤란로 123", "02-1234-5678");
        Kindergarten kg2 = createKindergarten("꿈나무 유치원", "서울시 서초구 강남대로 456", "02-9876-5432");

        // 2. 원장 2명 생성
        Member principal1 = createMember("principal1@test.com", "김원장", MemberRole.PRINCIPAL, kg1);
        Member principal2 = createMember("principal2@test.com", "이원장", MemberRole.PRINCIPAL, kg2);

        // 3. 선생님 4명 생성 (각 유치원 2명씩)
        Member teacher1 = createMember("teacher1@test.com", "김교사", MemberRole.TEACHER, kg1);
        Member teacher2 = createMember("teacher2@test.com", "박교사", MemberRole.TEACHER, kg1);
        Member teacher3 = createMember("teacher3@test.com", "최교사", MemberRole.TEACHER, kg2);
        Member teacher4 = createMember("teacher4@test.com", "정교사", MemberRole.TEACHER, kg2);

        // 4. 반 4개 생성 (각 유치원 2개씩)
        Classroom class1 = createClassroom(kg1, "해바라기반", "5세", teacher1);
        Classroom class2 = createClassroom(kg1, "장미반", "6세", teacher2);
        Classroom class3 = createClassroom(kg2, "나무반", "5세", teacher3);
        Classroom class4 = createClassroom(kg2, "꽃반", "6세", teacher4);

        // 5. 원아 12명 생성 (각 반 3명씩)
        List<Kid> kids = new ArrayList<>();
        kids.addAll(createKidsForClassroom(class1, "준우", "서윤", "도윤"));
        kids.addAll(createKidsForClassroom(class2, "시우", "하은", "지호"));
        kids.addAll(createKidsForClassroom(class3, "주원", "수빈", "지원"));
        kids.addAll(createKidsForClassroom(class4, "다은", "예준", "연우"));

        // 6. 부모님 12명 생성 (각 원아당 1명)
        int parentNum = 1;
        for (Kid kid : kids) {
            Member parent = createMember("parent" + parentNum + "@test.com", "학부모" + parentNum, MemberRole.PARENT, kid.getClassroom().getKindergarten());
            createParentKid(parent, kid, Relationship.FATHER);
            parentNum++;
        }

        // 7. 출석부 생성 (최근 7일간)
        LocalDate today = LocalDate.now();
        for (Kid kid : kids) {
            for (int i = 0; i < 7; i++) {
                LocalDate date = today.minusDays(i);
                createAttendance(kid, date);
            }
        }

        // 8. 알림장 생성 (각 반 2개씩)
        createNotepad(class1, teacher1, "오늘의 활동 안내", "오늘은 미술 시간에 그림 그리기 활동을 했습니다. 아이들이 참 재미있어하네요!", null);
        createNotepad(class1, teacher1, "주간 식단 안내", "이번 주 월요일: 김밥, 화요일: 비빔밥, 수요목: 돈가스, 금요일: 떡국", null);
        createNotepad(class2, teacher2, "현장 학습 안내", "다음 주 화요일은 과학관으로 현장 학습을 갑니다. 간편한 복장으로 와주세요.", null);
        createNotepad(class2, teacher2, "날씨에 따른 준비물", "내일은 비가 온다고 하니 우산을 꼭 챙겨주세요.", null);
        createNotepad(class3, teacher3, "체육 대회 연습", "이번 주부터 체육 대회 연습을 시작합니다. 운동화를 꼭 신어주세요.", null);
        createNotepad(class3, teacher3, "도서관 이용 안내", "매주 수요일은 도서관 날입니다. 도서 대출증을 챙겨주세요.", null);
        createNotepad(class4, teacher4, "음악 발표회", "이번 달 말에 음악 발표회가 있습니다. 악기 연습을 열심히 해주세요.", null);
        createNotepad(class4, teacher4, "비 오는 날 실내 놀이", "비가 오는 날은 실내에서 보드 게임과 블록 놀이를 합니다.", null);

        // 9. 공지사항 생성 (각 유치원 3개씩)
        createAnnouncement(kg1, principal1, "[긴급] 송파구 코로나19 확진자 동선 안내", "송파구에 코로나19 확진자 동선이 발생하여 이를 안내드립니다.", true);
        createAnnouncement(kg1, principal1, "5월 가정 통신문 발송 안내", "5월 가정 통신문을 오늘 발송하였습니다. 확인 부탁드립니다.", false);
        createAnnouncement(kg1, principal1, "여름 방학 일정 안내", "올해 여름 방학은 7월 20일부터 8월 20일까지입니다.", false);
        createAnnouncement(kg2, principal2, "[중요] 어린이잔치 행사 안내", "다음 주 5일 어린이날을 맞아 특별 행사가 준비되었습니다.", true);
        createAnnouncement(kg2, principal2, "새 학기 입학 안내", "2025학년도 새 학기 입학 원서 접수가 시작되었습니다.", false);
        createAnnouncement(kg2, principal2, "급식비 납부 안내", "이번 달 급식비를 5월 10일까지 납부부탁드립니다.", false);

        // 10. 원아별 알림장도 몇 개 생성
        createNotepad(class1, teacher1, "준우 생일 축하", "오늘 준우의 5번째 생일을 축하합니다! ", kids.get(0));
        createNotepad(class2, teacher2, "시우 칭찬 일기", "시우가 친구들과 사이좋게 지내는 모습이 아주 좋습니다.", kids.get(3));

        log.info("Dummy data loaded successfully!");
        log.info("Generated 2 kindergartens, 2 principals, 4 teachers, 4 classrooms, 12 kids, 12 parents, 84 attendance records, 10 notepads, 6 announcements");
    }

    private Kindergarten createKindergarten(String name, String address, String phone) {
        Kindergarten kg = Kindergarten.create(name, address, phone, LocalTime.of(9, 0), LocalTime.of(18, 0));
        return kindergartenRepository.save(kg);
    }

    private Member createMember(String email, String name, MemberRole role, Kindergarten kindergarten) {
        Member member = Member.create(email, passwordEncoder.encode("password123"), name, "010-1234-5678", role);
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
