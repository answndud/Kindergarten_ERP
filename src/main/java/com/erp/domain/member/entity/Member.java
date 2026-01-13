package com.erp.domain.member.entity;

import com.erp.domain.kindergarten.entity.Kindergarten;
import com.erp.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 엔티티
 */
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 이메일 (로그인 ID)
     */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * 비밀번호 (BCrypt 암호화)
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * 이름
     */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * 전화번호
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 역할 (PRINCIPAL, TEACHER, PARENT)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private MemberRole role;

    /**
     * 상태 (ACTIVE, INACTIVE, PENDING)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status = MemberStatus.PENDING;

    /**
     * 소속 유치원
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kindergarten_id")
    private Kindergarten kindergarten;

    /**
     * 탈퇴일 (Soft Delete)
     */
    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;

    // ========== 정적 팩토리 메서드 ==========

    /**
     * 회원 생성
     */
    public static Member create(String email, String encodedPassword,
                                String name, String phone, MemberRole role) {
        Member member = new Member();
        member.email = email;
        member.password = encodedPassword;
        member.name = name;
        member.phone = phone;
        member.role = role;
        member.status = MemberStatus.ACTIVE; // 기본 활성
        return member;
    }

    /**
     * 소셜 로그인 회원 생성 (비밀번호 없음)
     */
    public static Member createSocial(String email, String name,
                                      MemberRole role, String providerId) {
        Member member = new Member();
        member.email = email;
        member.name = name;
        member.role = role;
        member.status = MemberStatus.ACTIVE;
        return member;
    }

    // ========== 비즈니스 메서드 ==========

    /**
     * 프로필 수정
     */
    public void updateProfile(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    /**
     * 비밀번호 변경
     */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /**
     * 유치원 배정
     */
    public void assignKindergarten(Kindergarten kindergarten) {
        this.kindergarten = kindergarten;
    }

    /**
     * 회원 활성화
     */
    public void activate() {
        this.status = MemberStatus.ACTIVE;
    }

    /**
     * 회원 활성화 (별칭 메서드)
     */
    public void activateMember() {
        this.status = MemberStatus.ACTIVE;
    }

    /**
     * 회원 비활성화
     */
    public void deactivate() {
        this.status = MemberStatus.INACTIVE;
    }

    /**
     * 탈퇴 (Soft Delete)
     */
    public void withdraw() {
        this.status = MemberStatus.INACTIVE;
        this.deletedAt = java.time.LocalDateTime.now();
    }

    /**
     * 비밀번호 검증
     */
    public boolean matchesPassword(String rawPassword, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(rawPassword, this.password);
    }
}
