package com.erp.domain.member.service;

import com.erp.domain.member.entity.Member;
import com.erp.domain.member.entity.MemberRole;
import com.erp.domain.member.repository.MemberRepository;
import com.erp.global.exception.BusinessException;
import com.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 이메일 중복 확인
     */
    public boolean checkEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    /**
     * 회원가입
     */
    @Transactional
    public Long signUp(String email, String password, String name, String phone, MemberRole role) {
        // 이메일 중복 확인
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 회원 생성
        Member member = Member.create(email, encodedPassword, name, phone, role);

        // 저장
        Member savedMember = memberRepository.save(member);

        return savedMember.getId();
    }

    /**
     * 이메일로 회원 조회
     */
    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * ID로 회원 조회
     */
    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * 프로필 수정
     */
    @Transactional
    public void updateProfile(Long memberId, String name, String phone) {
        Member member = getMemberById(memberId);
        member.updateProfile(name, phone);
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(Long memberId, String oldPassword, String newPassword) {
        Member member = getMemberById(memberId);

        // 기존 비밀번호 검증
        if (!member.matchesPassword(oldPassword, passwordEncoder)) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 새 비밀번호 암호화 및 변경
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        member.changePassword(encodedNewPassword);
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void withdraw(Long memberId) {
        Member member = getMemberById(memberId);
        member.withdraw();
    }

    /**
     * 회원 활성화
     */
    @Transactional
    public void activateMember(Long memberId) {
        Member member = getMemberById(memberId);
        member.activate();
    }

    /**
     * 회원 비활성화
     */
    @Transactional
    public void deactivateMember(Long memberId) {
        Member member = getMemberById(memberId);
        member.deactivate();
    }
}
