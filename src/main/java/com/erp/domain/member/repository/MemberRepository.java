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

    /**
     * 유치원별 특정 역할 회원 조회 (원장 찾기 등)
     */
    @Query("SELECT m FROM Member m WHERE m.kindergarten.id = :kindergartenId AND m.role = :role AND m.deletedAt IS NULL")
    Optional<Member> findByKindergartenIdAndRole(@Param("kindergartenId") Long kindergartenId,
                                                  @Param("role") MemberRole role);

    /**
     * 유치원별 특정 역할 회원 목록
     */
    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.kindergarten k WHERE k.id = :kindergartenId AND m.role = :role AND m.deletedAt IS NULL")
    List<Member> findAllByKindergartenIdAndRole(@Param("kindergartenId") Long kindergartenId,
                                                   @Param("role") MemberRole role);

    /**
     * 유치원별 다중 역할 회원 목록
     */
    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.kindergarten k WHERE k.id = :kindergartenId AND m.role IN :roles AND m.deletedAt IS NULL")
    List<Member> findAllByKindergartenIdAndRoles(@Param("kindergartenId") Long kindergartenId,
                                                  @Param("roles") List<MemberRole> roles);

    /**
     * ID로 회원 조회 (유치원 포함)
     * 뷰에서 사용할 때 LazyInitializationException 방지용
     */
    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.kindergarten WHERE m.id = :id AND m.deletedAt IS NULL")
    Optional<Member> findByIdWithKindergarten(@Param("id") Long id);

    /**
     * 유치원별 특정 역할 회원 수
     */
    @Query("SELECT COUNT(m) FROM Member m WHERE m.kindergarten.id = :kindergartenId AND m.role = :role AND m.deletedAt IS NULL")
    long countByKindergartenIdAndRole(@Param("kindergartenId") Long kindergartenId, @Param("role") MemberRole role);

    /**
     * 유치원별 총 회원 수
     */
    @Query("SELECT COUNT(m) FROM Member m WHERE m.kindergarten.id = :kindergartenId AND m.deletedAt IS NULL")
    long countByKindergartenIdAndDeletedAtIsNull(@Param("kindergartenId") Long kindergartenId);
}
