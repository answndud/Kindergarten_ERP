package com.erp.common;

import com.erp.domain.member.entity.Member;
import com.erp.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected TestData testData;

    // Redis는 테스트에서 사용하지 않도록 mock 처리
    @MockBean
    protected RedisConnectionFactory redisConnectionFactory;

    protected Member principalMember;
    protected Member teacherMember;
    protected Member parentMember;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        principalMember = testData.createPrincipalMember();
        teacherMember = testData.createTeacherMember();
        parentMember = testData.createParentMember();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 인증된 사용자로 SecurityContext 설정
     */
    protected void setAuthentication(Member member) {
        UserDetails userDetails = User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())))
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
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
}
