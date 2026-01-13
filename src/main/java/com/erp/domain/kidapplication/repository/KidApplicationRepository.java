package com.erp.domain.kidapplication.repository;

import com.erp.domain.kidapplication.entity.ApplicationStatus;
import com.erp.domain.kidapplication.entity.KidApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KidApplicationRepository extends JpaRepository<KidApplication, Long> {

    /**
     * 학부모의 입학 신청 목록
     */
    List<KidApplication> findByParentIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long parentId);

    /**
     * 유치원별 대기 중인 입학 신청 목록
     */
    @Query("SELECT a FROM KidApplication a WHERE a.kindergarten.id = :kindergartenId AND a.status = 'PENDING' AND a.deletedAt IS NULL ORDER BY a.createdAt ASC")
    List<KidApplication> findPendingApplicationsByKindergartenId(@Param("kindergartenId") Long kindergartenId);

    /**
     * 특정 상태의 입학 신청 목록
     */
    List<KidApplication> findByParentIdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(Long parentId, ApplicationStatus status);

    /**
     * 유치원별 모든 입학 신청 목록
     */
    @Query("SELECT a FROM KidApplication a WHERE a.kindergarten.id = :kindergartenId AND a.deletedAt IS NULL ORDER BY a.createdAt DESC")
    List<KidApplication> findAllByKindergartenId(@Param("kindergartenId") Long kindergartenId);

    /**
     * 입학 신청 상세 조회
     */
    @Query("SELECT a FROM KidApplication a WHERE a.id = :applicationId AND a.deletedAt IS NULL")
    Optional<KidApplication> findByIdAndDeletedAtIsNull(@Param("applicationId") Long applicationId);
}
