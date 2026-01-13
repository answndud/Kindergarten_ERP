package com.erp.common;

import com.erp.domain.member.entity.Member;
import com.erp.domain.member.entity.MemberRole;
import com.erp.domain.member.entity.MemberStatus;
import com.erp.domain.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 테스트용 데이터 헬퍼 클래스
 */
public class TestData {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public TestData(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
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
     * 모든 테스트 데이터 정리
     */
    public void cleanup() {
        memberRepository.deleteAll();
    }
}
