package com.erp.domain.notepad.service;

import com.erp.domain.classroom.service.ClassroomService;
import com.erp.domain.kid.service.KidService;
import com.erp.domain.member.entity.Member;
import com.erp.domain.member.service.MemberService;
import com.erp.domain.notepad.dto.request.NotepadRequest;
import com.erp.domain.notepad.dto.response.NotepadDetailResponse;
import com.erp.domain.notepad.dto.response.NotepadResponse;
import com.erp.domain.notepad.entity.Notepad;
import com.erp.domain.notepad.entity.NotepadReadConfirm;
import com.erp.domain.notepad.repository.NotepadRepository;
import com.erp.global.exception.BusinessException;
import com.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 알림장 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotepadService {

    private final NotepadRepository notepadRepository;
    private final ClassroomService classroomService;
    private final KidService kidService;
    private final MemberService memberService;

    /**
     * 반별 알림장 생성
     */
    @Transactional
    public Long createClassroomNotepad(Long classroomId, Long writerId, String title, String content) {
        // 반 조회
        var classroom = classroomService.getClassroom(classroomId);
        // 작성자 조회
        Member writer = memberService.getMemberById(writerId);

        // 교사/원장 역할 확인
        validateWriterRole(writer);

        Notepad notepad = Notepad.createClassroomNotepad(classroom, writer, title, content);
        Notepad saved = notepadRepository.save(notepad);
        return saved.getId();
    }

    /**
     * 원생별 알림장 생성
     */
    @Transactional
    public Long createKidNotepad(Long kidId, Long writerId, String title, String content) {
        // 원생 조회
        var kid = kidService.getKid(kidId);
        // 작성자 조회
        Member writer = memberService.getMemberById(writerId);

        // 교사/원장 역할 확인
        validateWriterRole(writer);

        Notepad notepad = Notepad.createKidNotepad(kid, writer, title, content);
        Notepad saved = notepadRepository.save(notepad);
        return saved.getId();
    }

    /**
     * 전체 알림장 생성
     */
    @Transactional
    public Long createGlobalNotepad(Long writerId, String title, String content) {
        // 작성자 조회
        Member writer = memberService.getMemberById(writerId);

        // 원장 역할 확인
        if (writer.getRole() != com.erp.domain.member.entity.MemberRole.PRINCIPAL) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        Notepad notepad = Notepad.createGlobalNotepad(writer, title, content);
        Notepad saved = notepadRepository.save(notepad);
        return saved.getId();
    }

    /**
     * 알림장 생성 (요청 DTO 사용)
     */
    @Transactional
    public Long createNotepad(NotepadRequest request, Long writerId) {
        Member writer = memberService.getMemberById(writerId);
        validateWriterRole(writer);

        Notepad notepad;

        if (request.getKidId() != null) {
            // 원생별 알림장
            var kid = kidService.getKid(request.getKidId());
            notepad = Notepad.createKidNotepad(kid, writer, request.getTitle(), request.getContent());
        } else if (request.getClassroomId() != null) {
            // 반별 알림장
            var classroom = classroomService.getClassroom(request.getClassroomId());
            notepad = Notepad.createClassroomNotepad(classroom, writer, request.getTitle(), request.getContent());
        } else {
            // 전체 알림장
            if (writer.getRole() != com.erp.domain.member.entity.MemberRole.PRINCIPAL) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
            notepad = Notepad.createGlobalNotepad(writer, request.getTitle(), request.getContent());
        }

        // 사진 URL 설정
        if (request.getPhotoUrl() != null) {
            notepad.setPhotoUrls(request.getPhotoUrl());
        }

        Notepad saved = notepadRepository.save(notepad);
        return saved.getId();
    }

    /**
     * 알림장 조회
     */
    public Notepad getNotepad(Long id) {
        return notepadRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTEPAD_NOT_FOUND));
    }

    /**
     * 알림장 상세 조회 (읽음 확인 포함)
     */
    public NotepadDetailResponse getNotepadDetail(Long id, Long readerId) {
        Notepad notepad = getNotepad(id);
        List<NotepadReadConfirm> readConfirms = notepadRepository.findReadConfirmsByNotepadId(id);

        // 읽음 확인
        boolean isRead = false;
        if (readerId != null) {
            isRead = readConfirms.stream()
                    .anyMatch(rc -> rc.getReader().getId().equals(readerId));
        }

        return NotepadDetailResponse.from(notepad, readConfirms, isRead);
    }

    /**
     * 반별 알림장 목록 조회 (페이지)
     */
    public Page<NotepadResponse> getClassroomNotepads(Long classroomId, Pageable pageable) {
        // 반 존재 확인
        classroomService.getClassroom(classroomId);

        return notepadRepository.findClassroomNotepads(classroomId, pageable)
                .map(notepad -> NotepadResponse.from(notepad, 0));
    }

    /**
     * 원생별 알림장 목록 조회 (페이지)
     */
    public Page<NotepadResponse> getKidNotepads(Long kidId, Pageable pageable) {
        // 원생 존재 확인
        kidService.getKid(kidId);

        return notepadRepository.findKidNotepads(kidId, pageable)
                .map(notepad -> NotepadResponse.from(notepad, 0));
    }

    /**
     * 학부모용 알림장 목록 (반 전체 + 내 원생)
     */
    public Page<NotepadResponse> getNotepadsForParent(Long classroomId, Long kidId, Pageable pageable) {
        classroomService.getClassroom(classroomId);
        kidService.getKid(kidId);

        return notepadRepository.findNotepadsForParent(classroomId, kidId, pageable)
                .map(notepad -> {
                    int readCount = notepadRepository.findReadConfirmsByNotepadId(notepad.getId()).size();
                    return NotepadResponse.from(notepad, readCount);
                });
    }

    /**
     * 알림장 수정
     */
    @Transactional
    public void updateNotepad(Long id, NotepadRequest request, Long writerId) {
        Notepad notepad = getNotepad(id);

        // 작성자 확인
        if (!notepad.getWriter().getId().equals(writerId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        notepad.update(request.getTitle(), request.getContent());

        if (request.getPhotoUrl() != null) {
            notepad.setPhotoUrls(request.getPhotoUrl());
        }
    }

    /**
     * 알림장 삭제
     */
    @Transactional
    public void deleteNotepad(Long id, Long requesterId) {
        Notepad notepad = getNotepad(id);

        // 작성자 또는 원장만 삭제 가능
        Member requester = memberService.getMemberById(requesterId);
        if (!notepad.getWriter().getId().equals(requesterId) &&
            requester.getRole() != com.erp.domain.member.entity.MemberRole.PRINCIPAL) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        notepadRepository.delete(notepad);
    }

    /**
     * 알림장 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notepadId, Long readerId) {
        Notepad notepad = getNotepad(notepadId);
        Member reader = memberService.getMemberById(readerId);

        // 학부모 역할 확인
        if (reader.getRole() != com.erp.domain.member.entity.MemberRole.PARENT) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 이미 읽음 확인이 있는지 확인
        var existingConfirm = notepadRepository.findByNotepadIdAndReaderId(notepadId, readerId);

        if (existingConfirm.isEmpty()) {
            notepad.addReadConfirm(reader);
        } else {
            existingConfirm.get().updateReadTime();
        }
    }

    /**
     * 작성자 역할 확인 (교사 또는 원장)
     */
    private void validateWriterRole(Member writer) {
        if (writer.getRole() != com.erp.domain.member.entity.MemberRole.TEACHER &&
            writer.getRole() != com.erp.domain.member.entity.MemberRole.PRINCIPAL) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    /**
     * 알림장 Response 변환
     */
    public NotepadResponse toResponse(Notepad notepad) {
        int readCount = notepadRepository.findReadConfirmsByNotepadId(notepad.getId()).size();
        return NotepadResponse.from(notepad, readCount);
    }
}
