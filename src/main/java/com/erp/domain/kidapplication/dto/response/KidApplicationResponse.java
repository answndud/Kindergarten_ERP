package com.erp.domain.kidapplication.dto.response;

import com.erp.domain.kid.entity.Gender;
import com.erp.domain.kidapplication.entity.ApplicationStatus;
import com.erp.domain.kidapplication.entity.KidApplication;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record KidApplicationResponse(
        Long id,
        ParentInfo parent,
        KindergartenInfo kindergarten,
        String kidName,
        LocalDate birthDate,
        Gender gender,
        ClassroomInfo preferredClassroom,
        ApplicationStatus status,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime processedAt,
        String rejectionReason,
        Long kidId
) {
    public static KidApplicationResponse from(KidApplication application) {
        ClassroomInfo classroomInfo = null;
        if (application.getPreferredClassroom() != null) {
            classroomInfo = new ClassroomInfo(
                    application.getPreferredClassroom().getId(),
                    application.getPreferredClassroom().getName()
            );
        }

        return new KidApplicationResponse(
                application.getId(),
                new ParentInfo(application.getParent().getId(), application.getParent().getName()),
                new KindergartenInfo(application.getKindergarten().getId(), application.getKindergarten().getName()),
                application.getKidName(),
                application.getBirthDate(),
                application.getGender(),
                classroomInfo,
                application.getStatus(),
                application.getNotes(),
                application.getCreatedAt(),
                application.getProcessedAt(),
                application.getRejectionReason(),
                application.getKidId()
        );
    }

    public record ParentInfo(Long id, String name) {
    }

    public record KindergartenInfo(Long id, String name) {
    }

    public record ClassroomInfo(Long id, String name) {
    }
}
