package com.erp.domain.kid.service;

import com.erp.domain.classroom.entity.Classroom;
import com.erp.domain.classroom.service.ClassroomService;
import com.erp.domain.kid.dto.request.AssignParentRequest;
import com.erp.domain.kid.dto.request.KidRequest;
import com.erp.domain.kid.dto.request.UpdateClassroomRequest;
import com.erp.domain.kid.entity.Gender;
import com.erp.domain.kid.entity.Kid;
import com.erp.domain.kid.entity.ParentKid;
import com.erp.domain.kid.entity.Relationship;
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
 * 원생 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KidService {

    private final KidRepository kidRepository;
    private final ClassroomService classroomService;
    private final MemberService memberService;

    /**
     * 원생 생성
     */
    @Transactional
    public Long createKid(KidRequest request) {
        // 반 조회
        Classroom classroom = classroomService.getClassroom(request.getClassroomId());

        // 원생 생성
        Kid kid = Kid.create(
                classroom,
                request.getName(),
                request.getBirthDate(),
                request.getGender(),
                request.getAdmissionDate()
        );

        // 저장
        Kid saved = kidRepository.save(kid);

        return saved.getId();
    }

    /**
     * 원생 조회
     */
    public Kid getKid(Long id) {
        return kidRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.KID_NOT_FOUND));
    }

    /**
     * 반별 원생 목록 조회
     */
    public List<Kid> getKidsByClassroom(Long classroomId) {
        // 반 존재 확인
        classroomService.getClassroom(classroomId);

        return kidRepository.findByClassroomIdAndDeletedAtIsNull(classroomId);
    }

    /**
     * 유치원 원생 목록 조회
     */
    public List<Kid> getKidsByKindergarten(Long kindergartenId) {
        return kidRepository.findByKindergartenIdAndDeletedAtIsNull(kindergartenId);
    }

    /**
     * 반별 원생 목록 조회 (이름 검색)
     */
    public List<Kid> searchKidsByName(Long classroomId, String name) {
        // 반 존재 확인
        classroomService.getClassroom(classroomId);

        return kidRepository.findByClassroomIdAndNameContaining(classroomId, name);
    }

    /**
     * 유치원 원생 목록 조회 (이름 검색)
     */
    public List<Kid> searchKidsByKindergarten(Long kindergartenId, String name) {
        return kidRepository.findByKindergartenIdAndNameContaining(kindergartenId, name);
    }

    /**
     * 학부모의 원생 목록 조회
     */
    public List<Kid> getKidsByParent(Long parentId) {
        // 학부모 존재 확인
        Member parent = memberService.getMemberById(parentId);

        // 학부모 역할 확인
        if (parent.getRole() != com.erp.domain.member.entity.MemberRole.PARENT) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return kidRepository.findByParentId(parentId);
    }

    /**
     * 원생 수정
     */
    @Transactional
    public void updateKid(Long id, String name, java.time.LocalDate birthDate, Gender gender) {
        Kid kid = getKid(id);
        kid.update(name, birthDate, gender);
    }

    /**
     * 반 배정 변경
     */
    @Transactional
    public void updateClassroom(Long id, UpdateClassroomRequest request) {
        Kid kid = getKid(id);
        Classroom classroom = classroomService.getClassroom(request.getClassroomId());
        kid.assignClassroom(classroom);
    }

    /**
     * 학부모 연결
     */
    @Transactional
    public void assignParent(Long kidId, AssignParentRequest request) {
        Kid kid = getKid(kidId);
        Member parent = memberService.getMemberById(request.getParentId());

        // 학부모 역할 확인
        if (parent.getRole() != com.erp.domain.member.entity.MemberRole.PARENT) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 이미 연결되어 있는지 확인
        if (kid.hasParent(parent)) {
            throw new BusinessException(ErrorCode.PARENT_KID_RELATION_EXISTS);
        }

        kid.addParent(parent, request.getRelationship());
    }

    /**
     * 학부모 연결 해제
     */
    @Transactional
    public void removeParent(Long kidId, Long parentId) {
        Kid kid = getKid(kidId);
        Member parent = memberService.getMemberById(parentId);

        // 연결 확인
        if (!kid.hasParent(parent)) {
            throw new BusinessException(ErrorCode.PARENT_KID_RELATION_NOT_FOUND);
        }

        kid.removeParent(parent);
    }

    /**
     * 원생 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteKid(Long id) {
        Kid kid = getKid(id);
        kid.softDelete();
    }

    /**
     * 원생 상세 조회 (학부모 정보 포함)
     */
    public com.erp.domain.kid.dto.response.KidDetailResponse getKidDetail(Long id) {
        Kid kid = getKid(id);
        List<ParentKid> parentKids = kidRepository.findParentsByKidId(id);

        return com.erp.domain.kid.dto.response.KidDetailResponse.from(kid, parentKids);
    }
}
