package com.erp.domain.notepad.repository;

import com.erp.domain.notepad.entity.Notepad;
import com.erp.domain.notepad.entity.NotepadReadConfirm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 알림장 리포지토리
 */
@Repository
public interface NotepadRepository extends JpaRepository<Notepad, Long> {

    /**
     * 반별 알림장 목록 조회 (최신순)
     */
    @Query("SELECT n FROM Notepad n WHERE n.classroom.id = :classroomId AND n.kid IS NULL ORDER BY n.createdAt DESC")
    Page<Notepad> findClassroomNotepads(@Param("classroomId") Long classroomId, Pageable pageable);

    /**
     * 원생별 알림장 목록 조회 (최신순)
     */
    @Query("SELECT n FROM Notepad n WHERE n.kid.id = :kidId ORDER BY n.createdAt DESC")
    Page<Notepad> findKidNotepads(@Param("kidId") Long kidId, Pageable pageable);

    /**
     * 반별 + 원생별 알림장 (반 전체 + 내 원생)
     */
    @Query("SELECT n FROM Notepad n WHERE (n.classroom.id = :classroomId AND n.kid IS NULL) OR n.kid.id = :kidId ORDER BY n.createdAt DESC")
    Page<Notepad> findNotepadsForParent(@Param("classroomId") Long classroomId, @Param("kidId") Long kidId, Pageable pageable);

    /**
     * ID로 조회
     */
    @Query("SELECT n FROM Notepad n WHERE n.id = :id")
    Optional<Notepad> findById(@Param("id") Long id);

    /**
     * 특정 알림장의 읽음 확인 조회
     */
    @Query("SELECT rc FROM NotepadReadConfirm rc WHERE rc.notepad.id = :notepadId")
    List<NotepadReadConfirm> findReadConfirmsByNotepadId(@Param("notepadId") Long notepadId);

    /**
     * 특정 학부모가 읽은 알림장 목록
     */
    @Query("SELECT rc.notepad FROM NotepadReadConfirm rc WHERE rc.reader.id = :readerId ORDER BY rc.readAt DESC")
    Page<Notepad> findReadNotepadsByReader(@Param("readerId") Long readerId, Pageable pageable);

    /**
     * 특정 학부모가 특정 알림장을 읽었는지 확인
     */
    @Query("SELECT rc FROM NotepadReadConfirm rc WHERE rc.notepad.id = :notepadId AND rc.reader.id = :readerId")
    Optional<NotepadReadConfirm> findByNotepadIdAndReaderId(@Param("notepadId") Long notepadId, @Param("readerId") Long readerId);

    /**
     * 작성자별 알림장 목록
     */
    @Query("SELECT n FROM Notepad n WHERE n.writer.id = :writerId ORDER BY n.createdAt DESC")
    Page<Notepad> findByWriterId(@Param("writerId") Long writerId, Pageable pageable);
}
