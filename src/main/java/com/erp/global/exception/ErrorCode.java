package com.erp.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 에러 코드 enum
 * HTTP 상태 코드, 비즈니스 코드, 메시지를 포함합니다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ========== Common ==========
    INVALID_INPUT_VALUE(400, "C001", "잘못된 입력값입니다"),
    ENTITY_NOT_FOUND(404, "C002", "엔티티를 찾을 수 없습니다"),
    INTERNAL_SERVER_ERROR(500, "C003", "서버 오류가 발생했습니다"),
    METHOD_NOT_ALLOWED(405, "C004", "지원하지 않는 메서드입니다"),

    // ========== Auth ==========
    INVALID_CREDENTIALS(401, "A001", "이메일 또는 비밀번호가 잘못되었습니다"),
    TOKEN_EXPIRED(401, "A002", "토큰이 만료되었습니다"),
    TOKEN_INVALID(401, "A003", "유효하지 않은 토큰입니다"),
    ACCESS_DENIED(403, "A004", "접근 권한이 없습니다"),
    REFRESH_TOKEN_NOT_FOUND(404, "A005", "리프레시 토큰을 찾을 수 없습니다"),

    // ========== Member ==========
    EMAIL_ALREADY_EXISTS(409, "M001", "이미 사용 중인 이메일입니다"),
    MEMBER_NOT_FOUND(404, "M002", "회원을 찾을 수 없습니다"),
    MEMBER_WITHDRAWN(400, "M003", "이미 탈퇴한 회원입니다"),
    PASSWORD_MISMATCH(400, "M004", "비밀번호가 일치하지 않습니다"),

    // ========== Kindergarten ==========
    KINDERGARTEN_NOT_FOUND(404, "K001", "유치원을 찾을 수 없습니다"),
    KINDERGARTEN_ALREADY_EXISTS(409, "K002", "이미 등록된 유치원입니다"),

    // ========== Classroom ==========
    CLASSROOM_NOT_FOUND(404, "CL001", "반을 찾을 수 없습니다"),
    CLASSROOM_ALREADY_HAS_TEACHER(400, "CL002", "이미 담임 교사가 배정된 반입니다"),
    CLASSROOM_HAS_KIDS(400, "CL003", "원생이 있는 반은 삭제할 수 없습니다"),

    // ========== Kid ==========
    KID_NOT_FOUND(404, "KD001", "원생을 찾을 수 없습니다"),
    PARENT_KID_RELATION_EXISTS(409, "KD002", "이미 연결된 학부모-원생 관계입니다"),
    PARENT_KID_RELATION_NOT_FOUND(404, "KD003", "학부모-원생 연결을 찾을 수 없습니다"),

    // ========== Attendance ==========
    ATTENDANCE_ALREADY_EXISTS(409, "AT001", "이미 출석 정보가 존재합니다"),
    ATTENDANCE_NOT_FOUND(404, "AT002", "출석 정보를 찾을 수 없습니다"),
    INVALID_ATTENDANCE_STATUS(400, "AT003", "잘못된 출석 상태입니다"),

    // ========== Notepad ==========
    NOTEPAD_NOT_FOUND(404, "N001", "알림장을 찾을 수 없습니다"),
    NOTEPAD_ACCESS_DENIED(403, "N002", "알림장 조회 권한이 없습니다"),

    // ========== Announcement ==========
    ANNOUNCEMENT_NOT_FOUND(404, "AN001", "공지사항을 찾을 수 없습니다");

    /**
     * HTTP 상태 코드
     */
    private final int status;

    /**
     * 비즈니스 에러 코드
     */
    private final String code;

    /**
     * 에러 메시지
     */
    private final String message;
}
