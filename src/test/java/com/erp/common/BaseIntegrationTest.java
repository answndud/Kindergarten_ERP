package com.erp.common;

import com.erp.domain.member.entity.Member;
import com.erp.domain.member.repository.MemberRepository;
import com.erp.global.security.user.CustomUserDetails;
import com.erp.domain.kindergarten.entity.Kindergarten;
import com.erp.domain.kindergarten.repository.KindergartenRepository;
import com.erp.domain.classroom.entity.Classroom;
import com.erp.domain.classroom.repository.ClassroomRepository;
import com.erp.domain.kid.entity.Gender;
import com.erp.domain.kid.entity.Kid;
import com.erp.domain.kid.entity.Relationship;
import com.erp.domain.kid.entity.ParentKid;
import com.erp.domain.kid.repository.KidRepository;
import com.erp.domain.kid.repository.ParentKidRepository;
import com.erp.domain.attendance.entity.Attendance;
import com.erp.domain.attendance.entity.AttendanceStatus;
import com.erp.domain.attendance.repository.AttendanceRepository;
import com.erp.domain.notepad.entity.Notepad;
import com.erp.domain.notepad.repository.NotepadRepository;
import com.erp.domain.announcement.entity.Announcement;
import com.erp.domain.announcement.repository.AnnouncementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.mockito.Mockito;

import java.util.Collections;

/**
 * 통합 테스트 기반 클래스
 * 모든 통합 테스트가 상속받아 사용하는 공통 설정 포함
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected KindergartenRepository kindergartenRepository;

    @Autowired
    protected ClassroomRepository classroomRepository;

    @Autowired
    protected KidRepository kidRepository;

    @Autowired
    protected ParentKidRepository parentKidRepository;

    @Autowired
    protected AttendanceRepository attendanceRepository;

    @Autowired
    protected NotepadRepository notepadRepository;

    @Autowired
    protected AnnouncementRepository announcementRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected TestData testData;

    // Redis는 테스트에서 사용하지 않도록 mock 처리
    @MockBean
    protected RedisConnectionFactory redisConnectionFactory;

    @MockBean
    protected RedisTemplate<String, Object> redisTemplate;

    protected Member principalMember;
    protected Member teacherMember;
    protected Member parentMember;
    protected Kindergarten kindergarten;
    protected Classroom classroom;
    protected Kid kid;
    protected Attendance attendance;
    protected Notepad notepad;
    protected Announcement announcement;

    @BeforeEach
    void setUp() {
        testData.cleanup();
        resetIdentity();

        ValueOperations<String, Object> valueOperations = Mockito.mock(ValueOperations.class);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(redisTemplate.hasKey(Mockito.anyString())).thenReturn(true);
        Mockito.when(redisTemplate.keys(Mockito.anyString())).thenReturn(Collections.emptySet());
        Mockito.when(redisTemplate.delete(Mockito.anyCollection())).thenReturn(0L);
        Mockito.when(redisTemplate.delete(Mockito.anyString())).thenReturn(true);

        // 테스트 데이터 초기화
        principalMember = testData.createPrincipalMember();
        teacherMember = testData.createTeacherMember();
        parentMember = testData.createParentMember();

        kindergarten = testData.createKindergarten();
        principalMember.assignKindergarten(kindergarten);
        teacherMember.assignKindergarten(kindergarten);
        parentMember.assignKindergarten(kindergarten);
        memberRepository.save(principalMember);
        memberRepository.save(teacherMember);
        memberRepository.save(parentMember);

        classroom = testData.createClassroom(kindergarten);
        classroom.assignTeacher(teacherMember);
        classroomRepository.save(classroom);

        kid = Kid.create(classroom, "테스트 원생", java.time.LocalDate.of(2020, 1, 1),
                Gender.MALE, java.time.LocalDate.now());
        kidRepository.save(kid);

        kid.addParent(parentMember, Relationship.MOTHER);
        kidRepository.save(kid);

        attendance = Attendance.create(kid, java.time.LocalDate.of(2025, 1, 13), AttendanceStatus.PRESENT);
        attendance.recordDropOff(java.time.LocalTime.of(9, 0));
        attendance.recordPickUp(java.time.LocalTime.of(16, 0));
        attendanceRepository.save(attendance);

        notepad = Notepad.createClassroomNotepad(classroom, teacherMember, "테스트 알림장", "테스트 내용");
        notepadRepository.save(notepad);

        announcement = Announcement.create(kindergarten, principalMember, "테스트 공지", "테스트 공지 내용");
        announcementRepository.save(announcement);

        replaceAuthenticationPrincipal();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 인증된 사용자로 SecurityContext 설정
     */
    protected void setAuthentication(Member member) {
        CustomUserDetails customUserDetails = new CustomUserDetails(member);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities())
        );
    }

    /**
     * 원장 인증 상태 설정
     */
    protected void authenticateAsPrincipal() {
        setAuthentication(principalMember);
    }

    /**
     * 교사 인증 상태 설정
     */
    protected void authenticateAsTeacher() {
        setAuthentication(teacherMember);
    }

    /**
     * 학부모 인증 상태 설정
     */
    protected void authenticateAsParent() {
        setAuthentication(parentMember);
    }

    /**
     * 인증 해제
     */
    protected void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private void replaceAuthenticationPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return;
        }
        if (!(principal instanceof UserDetails userDetails)) {
            return;
        }

        memberRepository.findByEmail(userDetails.getUsername())
                .ifPresent(member -> {
                    CustomUserDetails customUserDetails = new CustomUserDetails(member);
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(
                                    customUserDetails,
                                    null,
                                    customUserDetails.getAuthorities()
                            )
                    );
                });
    }

    private void resetIdentity() {
        resetTableIdentity("attendance");
        resetTableIdentity("notepad");
        resetTableIdentity("announcement");
        resetTableIdentity("parent_kid");
        resetTableIdentity("kid");
        resetTableIdentity("classroom");
        resetTableIdentity("kindergarten");
        resetTableIdentity("member");
    }

    private void resetTableIdentity(String tableName) {
        String normalized = tableName.toUpperCase();
        jdbcTemplate.execute("ALTER TABLE " + normalized + " ALTER COLUMN ID RESTART WITH 1");
    }
}
