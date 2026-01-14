package com.erp.domain.kidapplication.entity;

import com.erp.domain.classroom.entity.Classroom;
import com.erp.domain.kindergarten.entity.Kindergarten;
import com.erp.domain.kid.entity.Gender;
import com.erp.domain.member.entity.Member;
import com.erp.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "kid_application")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KidApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 학부모 (신청자)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private Member parent;

    /**
     * 대상 유치원
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kindergarten_id", nullable = false)
    private Kindergarten kindergarten;

    /**
     * 원생 이름
     */
    @Column(name = "kid_name", nullable = false, length = 50)
    private String kidName;

    /**
     * 생년월일
     */
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    /**
     * 성별
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    /**
     * 희망 반
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_classroom_id")
    private Classroom preferredClassroom;

    /**
     * 신청 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApplicationStatus status;

    /**
     * 특이사항
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * 처리 일시
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * 거절 사유
     */
    @Column(name = "rejection_reason")
    private String rejectionReason;

    /**
     * 처리자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private Member processedBy;

    /**
     * 승인 후 생성된 Kid ID
     */
    @Column(name = "kid_id")
    private Long kidId;

    /**
     * 삭제일 (Soft Delete)
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private KidApplication(Member parent, Kindergarten kindergarten, String kidName,
                          LocalDate birthDate, Gender gender, Classroom preferredClassroom,
                          String notes) {
        this.parent = parent;
        this.kindergarten = kindergarten;
        this.kidName = kidName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.preferredClassroom = preferredClassroom;
        this.notes = notes;
        this.status = ApplicationStatus.PENDING;
    }

    /**
     * 입학 신청 승인
     */
    public void approve(Classroom classroom, Member processor, Long kidId) {
        if (!status.isProcessable()) {
            throw new IllegalStateException("처리 가능한 상태가 아닙니다: " + status);
        }
        this.status = ApplicationStatus.APPROVED;
        this.preferredClassroom = classroom;
        this.processedAt = LocalDateTime.now();
        this.processedBy = processor;
        this.kidId = kidId;
    }

    /**
     * 입학 신청 거절
     */
    public void reject(String reason, Member processor) {
        if (!status.isProcessable()) {
            throw new IllegalStateException("처리 가능한 상태가 아닙니다: " + status);
        }
        this.status = ApplicationStatus.REJECTED;
        this.rejectionReason = reason;
        this.processedAt = LocalDateTime.now();
        this.processedBy = processor;
    }

    /**
     * 입학 신청 취소
     */
    public void cancel() {
        if (!status.isProcessable()) {
            throw new IllegalStateException("취소 가능한 상태가 아닙니다: " + status);
        }
        this.status = ApplicationStatus.CANCELLED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 취소/거절된 입학 신청 재신청
     */
    public void reapply(Kindergarten kindergarten, String kidName, LocalDate birthDate, Gender gender,
                        Classroom preferredClassroom, String notes) {
        if (!(status.isCancelled() || status.isRejected())) {
            throw new IllegalStateException("재신청 가능한 상태가 아닙니다: " + status);
        }

        this.kindergarten = kindergarten;
        this.kidName = kidName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.preferredClassroom = preferredClassroom;
        this.notes = notes;

        this.status = ApplicationStatus.PENDING;
        this.processedAt = null;
        this.processedBy = null;
        this.rejectionReason = null;
        this.kidId = null;
    }

    /**
     * Soft Delete
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 대기 상태 확인
     */
    public boolean isPending() {
        return status.isPending();
    }

    /**
     * 승인 완료 확인
     */
    public boolean isApproved() {
        return status.isApproved();
    }

    /**
     * 거절 확인
     */
    public boolean isRejected() {
        return status.isRejected();
    }

    /**
     * 입학 신청 생성 정적 팩토리 메서드
     */
    public static KidApplication create(Member parent, Kindergarten kindergarten,
                                        String kidName, LocalDate birthDate, Gender gender,
                                        Classroom preferredClassroom, String notes) {
        return KidApplication.builder()
                .parent(parent)
                .kindergarten(kindergarten)
                .kidName(kidName)
                .birthDate(birthDate)
                .gender(gender)
                .preferredClassroom(preferredClassroom)
                .notes(notes)
                .build();
    }
}
