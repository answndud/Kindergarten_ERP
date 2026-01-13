package com.erp.domain.classroom.service;

import com.erp.domain.classroom.entity.Classroom;
import com.erp.domain.classroom.repository.ClassroomRepository;
import com.erp.domain.kindergarten.entity.Kindergarten;
import com.erp.domain.kindergarten.service.KindergartenService;
import com.erp.domain.kid.repository.KidRepository;
import com.erp.domain.member.entity.Member;
import com.erp.domain.member.service.MemberService;
import com.erp.global.exception.BusinessException;
import com.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 반 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final KindergartenService kindergartenService;
    private final MemberService memberService;
    private final KidRepository kidRepository;

    /**
     * 반 생성
     */
    @Transactional
    public Long createClassroom(Long kindergartenId, String name, String ageGroup) {
        // 유치원 조회
        Kindergarten kindergarten = kindergartenService.getKindergarten(kindergartenId);

        // 반 생성
        Classroom classroom = Classroom.create(kindergarten, name, ageGroup);

        // 저장
        Classroom saved = classroomRepository.save(classroom);

        return saved.getId();
    }

    /**
     * 반 조회
     */
    public Classroom getClassroom(Long id) {
        return classroomRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND));
    }

    /**
     * 유치원별 반 목록 조회
     */
    public List<Classroom> getClassroomsByKindergarten(Long kindergartenId) {
        // 유치원 존재 확인
        kindergartenService.getKindergarten(kindergartenId);

        return classroomRepository.findByKindergartenIdAndDeletedAtIsNull(kindergartenId);
    }

    /**
     * 반 수정
     */
    @Transactional
    public void updateClassroom(Long id, String name, String ageGroup) {
        Classroom classroom = getClassroom(id);
        classroom.update(name, ageGroup);
    }

    /**
     * 반 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteClassroom(Long id) {
        Classroom classroom = getClassroom(id);

        // 원생이 있는지 확인
        long kidsCount = kidRepository.countByClassroomIdAndDeletedAtIsNull(id);
        if (!classroom.canDelete(kidsCount)) {
            throw new BusinessException(ErrorCode.CLASSROOM_HAS_KIDS);
        }

        classroom.softDelete();
    }

    /**
     * 담임 교사 배정
     */
    @Transactional
    public void assignTeacher(Long classroomId, Long teacherId) {
        Classroom classroom = getClassroom(classroomId);
        Member teacher = memberService.getMemberById(teacherId);

        // 교사 역할 확인
        if (teacher.getRole() != com.erp.domain.member.entity.MemberRole.TEACHER &&
            teacher.getRole() != com.erp.domain.member.entity.MemberRole.PRINCIPAL) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 이미 담임 교사가 있는지 확인
        if (!classroom.canAssignTeacher()) {
            throw new BusinessException(ErrorCode.CLASSROOM_ALREADY_HAS_TEACHER);
        }

        classroom.assignTeacher(teacher);
    }

    /**
     * 담임 교사 해제
     */
    @Transactional
    public void removeTeacher(Long classroomId) {
        Classroom classroom = getClassroom(classroomId);
        classroom.removeTeacher();
    }
}
