package com.erp.domain.kid.repository;

import com.erp.domain.kid.entity.Kid;
import com.erp.domain.kid.entity.ParentKid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 원생 리포지토리
 */
@Repository
public interface KidRepository extends JpaRepository<Kid, Long> {

    /**
     * 반별 원생 목록 조회 (삭제되지 않은 원생만)
     */
    @Query("SELECT k FROM Kid k JOIN FETCH k.classroom WHERE k.classroom.id = :classroomId AND k.deletedAt IS NULL ORDER BY k.name")
    List<Kid> findByClassroomIdAndDeletedAtIsNull(@Param("classroomId") Long classroomId);

    /**
     * ID로 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT k FROM Kid k JOIN FETCH k.classroom WHERE k.id = :id AND k.deletedAt IS NULL")
    Optional<Kid> findByIdAndDeletedAtIsNull(@Param("id") Long id);

    /**
     * 반별 원생 수 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT COUNT(k) FROM Kid k WHERE k.classroom.id = :classroomId AND k.deletedAt IS NULL")
    long countByClassroomIdAndDeletedAtIsNull(@Param("classroomId") Long classroomId);

    /**
     * 이름으로 원생 검색 (반별)
     */
    @Query("SELECT k FROM Kid k JOIN FETCH k.classroom WHERE k.classroom.id = :classroomId AND k.deletedAt IS NULL AND k.name LIKE %:name% ORDER BY k.name")
    List<Kid> findByClassroomIdAndNameContaining(@Param("classroomId") Long classroomId, @Param("name") String name);

    /**
     * 특정 학부모의 원생 목록 조회
     */
    @Query("SELECT k FROM ParentKid pk JOIN pk.kid k JOIN FETCH k.classroom WHERE pk.parent.id = :parentId AND k.deletedAt IS NULL ORDER BY k.name")
    List<Kid> findByParentId(@Param("parentId") Long parentId);

    /**
     * 특정 학부모의 특정 원생 연결 조회
     */
    @Query("SELECT pk FROM ParentKid pk WHERE pk.parent.id = :parentId AND pk.kid.id = :kidId")
    Optional<ParentKid> findParentKidByParentIdAndKidId(@Param("parentId") Long parentId, @Param("kidId") Long kidId);

    /**
     * 원생의 모든 학부모 연결 조회
     */
    @Query("SELECT pk FROM ParentKid pk WHERE pk.kid.id = :kidId")
    List<ParentKid> findParentsByKidId(@Param("kidId") Long kidId);
}
