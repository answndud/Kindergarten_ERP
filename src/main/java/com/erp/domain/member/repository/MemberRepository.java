package com.erp.domain.member.repository;

import com.erp.domain.member.entity.Member;
import com.erp.domain.member.entity.MemberRole;
import com.erp.domain.member.entity.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 회원 리포지토리
 */
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    /**
     * 이메일로 회원 조회
     */
    Optional<Member> findByEmail(String email);

    /**
     * 이메일 중복 확인
     */
    boolean existsByEmail(String email);

    /**
     * 이메일과 활성 상태로 회원 조회
     */
    Optional<Member> findByEmailAndStatus(String email, MemberStatus status);

    /**
     * 역할별 회원 목록 조회
     */
    List<Member> findByRole(MemberRole role);

    /**
     * 유치원별 회원 목록 조회
     */
    @Query("SELECT m FROM Member m WHERE m.kindergarten.id = :kindergartenId AND m.status = :status")
    List<Member> findByKindergartenIdAndStatus(@Param("kindergartenId") Long kindergartenId,
                                                @Param("status") MemberStatus status);
}
