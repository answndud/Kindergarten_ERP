package com.erp.domain.kidapplication.service;

import com.erp.domain.classroom.entity.Classroom;
import com.erp.domain.classroom.repository.ClassroomRepository;
import com.erp.domain.kindergarten.entity.Kindergarten;
import com.erp.domain.kindergarten.repository.KindergartenRepository;
import com.erp.domain.kid.entity.Gender;
import com.erp.domain.kid.entity.Kid;
import com.erp.domain.kid.entity.ParentKid;
import com.erp.domain.kid.repository.KidRepository;
import com.erp.domain.kid.repository.ParentKidRepository;
import com.erp.domain.kidapplication.dto.request.ApproveKidApplicationRequest;
import com.erp.domain.kidapplication.dto.request.KidApplicationRequest;
import com.erp.domain.kidapplication.dto.request.RejectRequest;
import com.erp.domain.kidapplication.dto.response.KidApplicationResponse;
import com.erp.domain.kidapplication.entity.ApplicationStatus;
import com.erp.domain.kidapplication.entity.KidApplication;
import com.erp.domain.kidapplication.repository.KidApplicationRepository;
import com.erp.domain.member.entity.Member;
import com.erp.domain.member.entity.MemberRole;
import com.erp.domain.member.entity.MemberStatus;
import com.erp.domain.member.repository.MemberRepository;
import com.erp.domain.notification.entity.NotificationType;
import com.erp.domain.notification.service.NotificationService;
import com.erp.global.exception.BusinessException;
import com.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KidApplicationService {

    private final KidApplicationRepository applicationRepository;
    private final MemberRepository memberRepository;
    private final KindergartenRepository kindergartenRepository;
    private final ClassroomRepository classroomRepository;
    private final KidRepository kidRepository;
    private final ParentKidRepository parentKidRepository;
    private final NotificationService notificationService;

    /**
     * 학부모가 원생 입학 신청
     */
    @Transactional
    public Long apply(KidApplicationRequest request, Long parentId) {
        Member parent = memberRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (parent.getRole() != MemberRole.PARENT) {
            throw new BusinessException(ErrorCode.INVALID_MEMBER_ROLE);
        }

        // 이미 유치원에 배정된 학부모는 신청 불가
        if (parent.getKindergarten() != null) {
            throw new BusinessException(ErrorCode.ALREADY_ASSIGNED_TO_KINDERGARTEN);
        }

        Kindergarten kindergarten = kindergartenRepository.findById(request.kindergartenId())
                .orElseThrow(() -> new BusinessException(ErrorCode.KINDERGARTEN_NOT_FOUND));

        Classroom preferredClassroom = null;
        if (request.preferredClassroomId() != null) {
            preferredClassroom = classroomRepository.findById(request.preferredClassroomId())
                    .orElse(null);
        }

        // 동일 유치원에 이미 대기 중인 신청이 있으면 불가
        applicationRepository.findPendingApplicationByParentAndKindergarten(parentId, request.kindergartenId())
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorCode.APPLICATION_ALREADY_EXISTS);
                });

        // 다른 유치원 포함 "대기 중" 신청이 있으면 추가 신청 불가
        if (applicationRepository.existsByParentIdAndStatusAndDeletedAtIsNull(parentId, ApplicationStatus.PENDING)) {
            throw new BusinessException(ErrorCode.PENDING_APPLICATION_EXISTS);
        }

        // 기존 취소/거절 신청이 있으면 재신청(UPDATE)
        KidApplication saved;
        var existing = applicationRepository.findByParentAndKindergarten(parentId, request.kindergartenId());
        if (existing.isPresent()) {
            KidApplication application = existing.get();
            if (application.getStatus().isCancelled() || application.getStatus().isRejected()) {
                application.reapply(kindergarten, request.kidName(), request.birthDate(), request.gender(), preferredClassroom, request.notes());
                saved = applicationRepository.save(application);
            } else {
                throw new BusinessException(ErrorCode.APPLICATION_ALREADY_EXISTS);
            }
        } else {
            KidApplication application = KidApplication.create(
                    parent, kindergarten, request.kidName(), request.birthDate(),
                    request.gender(), preferredClassroom, request.notes()
            );

            try {
                saved = applicationRepository.save(application);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                throw new BusinessException(ErrorCode.APPLICATION_ALREADY_EXISTS);
            }
        }

        // 신청 시점부터 승인 전까지는 PENDING으로 고정
        if (parent.getStatus() != MemberStatus.PENDING) {
            parent.markPending();
        }

        // 유치원 원장/교사에게 알림 발송
        notifyStaffAboutApplication(kindergarten, parent, request.kidName());

        return saved.getId();
    }

    /**
     * 입학 신청 승인
     */
    @Transactional
    public void approve(Long applicationId, ApproveKidApplicationRequest request, Long processorId) {
        KidApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.isPending()) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_PENDING);
        }

        Member processor = memberRepository.findById(processorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 권한 확인: 해당 유치원의 원장 또는 교사
        if (processor.getKindergarten() == null ||
                !processor.getKindergarten().getId().equals(application.getKindergarten().getId())) {
            throw new BusinessException(ErrorCode.KINDERGARTEN_ACCESS_DENIED);
        }

        Classroom classroom = classroomRepository.findById(request.classroomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND));

        // 반이 해당 유치원에 속하는지 확인
        if (!classroom.getKindergarten().getId().equals(application.getKindergarten().getId())) {
            throw new BusinessException(ErrorCode.CLASSROOM_NOT_BELONG_TO_KINDERGARTEN);
        }

        // Kid 엔티티 생성
        Kid kid = Kid.create(
                classroom,
                application.getKidName(),
                application.getBirthDate(),
                application.getGender(),
                LocalDate.now()  // 입학일은 오늘
        );
        Kid savedKid = kidRepository.save(kid);

        // ParentKid 관계 생성
        ParentKid parentKid = ParentKid.create(kid, application.getParent(), request.relationshipOrDefault());
        parentKidRepository.save(parentKid);

        // 입학 신청 승인 처리
        application.approve(classroom, processor, savedKid.getId());

        // 학부모에게 유치원 배정 (아직 배정되지 않은 경우)
        if (application.getParent().getKindergarten() == null) {
            application.getParent().assignKindergarten(application.getKindergarten());
        }
        application.getParent().activateMember();

        // 학부모에게 알림 발송
        notifyParentAboutApproval(application.getParent(), application.getKidName(), application.getKindergarten());
    }

    /**
     * 입학 신청 거절
     */
    @Transactional
    public void reject(Long applicationId, RejectRequest request, Long processorId) {
        KidApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.isPending()) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_PENDING);
        }

        Member processor = memberRepository.findById(processorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 권한 확인
        if (processor.getKindergarten() == null ||
                !processor.getKindergarten().getId().equals(application.getKindergarten().getId())) {
            throw new BusinessException(ErrorCode.KINDERGARTEN_ACCESS_DENIED);
        }

        application.reject(request.reason(), processor);

        // 학부모에게 알림 발송
        notifyParentAboutRejection(application.getParent(), application.getKidName(), application.getKindergarten(), request.reason());
    }

    /**
     * 입학 신청 취소
     */
    @Transactional
    public void cancel(Long applicationId, Long parentId) {
        KidApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.getParent().getId().equals(parentId)) {
            throw new BusinessException(ErrorCode.APPLICATION_ACCESS_DENIED);
        }

        application.cancel();

        // 유치원 원장/교사에게 알림 발송
        Kindergarten kindergarten = application.getKindergarten();
        if (kindergarten != null) {
            notifyStaffAboutCancellation(kindergarten, application.getParent(), application.getKidName());
        }
    }

    /**
     * 유치원별 대기 중인 입학 신청 목록
     */
    @Transactional(readOnly = true)
    public List<KidApplicationResponse> getPendingApplications(Long kindergartenId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 권한 확인: 해당 유치원의 원장 또는 교사
        if (member.getKindergarten() == null || !member.getKindergarten().getId().equals(kindergartenId)) {
            throw new BusinessException(ErrorCode.KINDERGARTEN_ACCESS_DENIED);
        }

        if (member.getRole() != MemberRole.PRINCIPAL && member.getRole() != MemberRole.TEACHER) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        List<KidApplication> applications = applicationRepository.findPendingApplicationsByKindergartenId(kindergartenId);
        return applications.stream()
                .map(KidApplicationResponse::from)
                .toList();
    }

    /**
     * 학부모의 입학 신청 목록
     */
    @Transactional(readOnly = true)
    public List<KidApplicationResponse> getMyApplications(Long parentId) {
        List<KidApplication> applications = applicationRepository.findByParentIdAndDeletedAtIsNullOrderByCreatedAtDesc(parentId);
        return applications.stream()
                .map(KidApplicationResponse::from)
                .toList();
    }

    /**
     * 입학 신청 상세 조회
     */
    @Transactional(readOnly = true)
    public KidApplicationResponse getApplication(Long applicationId, Long memberId) {
        KidApplication application = applicationRepository.findByIdAndDeletedAtIsNull(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 권한 확인: 본인 또는 해당 유치원의 원장/교사
        boolean hasAccess = application.getParent().getId().equals(memberId) ||
                (member.getKindergarten() != null && member.getKindergarten().getId().equals(application.getKindergarten().getId()));

        if (!hasAccess) {
            throw new BusinessException(ErrorCode.APPLICATION_ACCESS_DENIED);
        }

        return KidApplicationResponse.from(application);
    }

    private void notifyStaffAboutApplication(Kindergarten kindergarten, Member parent, String kidName) {
        String content = parent.getName() + " 학부모님이 자녀(" + kidName + ")의 입학을 신청했습니다.";

        // 원장에게 알림
        memberRepository.findByKindergartenIdAndRole(kindergarten.getId(), MemberRole.PRINCIPAL)
                .ifPresent(principal -> notificationService.notifyWithLink(
                        principal.getId(),
                        NotificationType.KID_APPLICATION_SUBMITTED,
                        "새로운 입학 신청",
                        content,
                        "/applications/pending"
                ));

        // 교사들에게도 알림
        memberRepository.findAllByKindergartenIdAndRole(kindergarten.getId(), MemberRole.TEACHER)
                .forEach(teacher -> notificationService.notifyWithLink(
                        teacher.getId(),
                        NotificationType.KID_APPLICATION_SUBMITTED,
                        "새로운 입학 신청",
                        content,
                        "/applications/pending"
                ));
    }

    private void notifyParentAboutApproval(Member parent, String kidName, Kindergarten kindergarten) {
        notificationService.notifyWithLink(
                parent.getId(),
                NotificationType.KID_APPLICATION_APPROVED,
                "입학 승인",
                kidName + "의 " + kindergarten.getName() + " 입학이 승인되었습니다.",
                "/applications/pending"
        );
    }

    private void notifyParentAboutRejection(Member parent, String kidName, Kindergarten kindergarten, String reason) {
        String content = kidName + "의 " + kindergarten.getName() + " 입학이 거절되었습니다.";
        if (reason != null && !reason.isEmpty()) {
            content += "\n사유: " + reason;
        }
        notificationService.notifyWithLink(
                parent.getId(),
                NotificationType.KID_APPLICATION_REJECTED,
                "입학 거절",
                content,
                "/applications/pending"
        );
    }

    private void notifyStaffAboutCancellation(Kindergarten kindergarten, Member parent, String kidName) {
        String content = parent.getName() + " 학부모님이 자녀(" + kidName + ")의 입학 신청을 취소했습니다.";

        memberRepository.findByKindergartenIdAndRole(kindergarten.getId(), MemberRole.PRINCIPAL)
                .ifPresent(principal -> notificationService.notify(
                        principal.getId(),
                        NotificationType.KID_APPLICATION_CANCELLED,
                        "입학 신청 취소",
                        content
                ));

        memberRepository.findAllByKindergartenIdAndRole(kindergarten.getId(), MemberRole.TEACHER)
                .forEach(teacher -> notificationService.notify(
                        teacher.getId(),
                        NotificationType.KID_APPLICATION_CANCELLED,
                        "입학 신청 취소",
                        content
                ));
    }
}
