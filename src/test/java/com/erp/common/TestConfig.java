package com.erp.common;

import com.erp.domain.classroom.repository.ClassroomRepository;
import com.erp.domain.kindergarten.repository.KindergartenRepository;
import com.erp.domain.kid.repository.KidRepository;
import com.erp.domain.member.repository.MemberRepository;
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
                         KidRepository kidRepository) {
        return new TestData(memberRepository, passwordEncoder, kindergartenRepository, classroomRepository, kidRepository);
    }
}
