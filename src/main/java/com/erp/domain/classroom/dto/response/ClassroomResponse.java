package com.erp.domain.classroom.dto.response;

import java.time.LocalDateTime;

/**
 * 반 정보 응답 DTO
 */
public record ClassroomResponse(
        Long id,
        Long kindergartenId,
        String kindergartenName,
        String name,
        String ageGroup,
        Long teacherId,
        String teacherName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Classroom 엔티티를 DTO로 변환
     */
    public static ClassroomResponse from(com.erp.domain.classroom.entity.Classroom classroom) {
        Long teacherId = null;
        String teacherName = null;

        if (classroom.getTeacher() != null) {
            teacherId = classroom.getTeacher().getId();
            teacherName = classroom.getTeacher().getName();
        }

        return new ClassroomResponse(
                classroom.getId(),
                classroom.getKindergarten().getId(),
                classroom.getKindergarten().getName(),
                classroom.getName(),
                classroom.getAgeGroup(),
                teacherId,
                teacherName,
                classroom.getCreatedAt(),
                classroom.getUpdatedAt()
        );
    }
}
