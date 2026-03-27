package com.erp.domain.kidapplication.service;

import com.erp.domain.classroom.entity.Classroom;
import com.erp.domain.classroom.service.ClassroomCapacityService;
import com.erp.domain.domainaudit.entity.DomainAuditAction;
import com.erp.domain.domainaudit.entity.DomainAuditTargetType;
import com.erp.domain.domainaudit.service.DomainAuditLogService;
import com.erp.domain.kindergarten.entity.Kindergarten;
import com.erp.domain.kindergarten.repository.KindergartenRepository;
import com.erp.domain.kid.entity.Kid;
import com.erp.domain.kid.entity.ParentKid;
import com.erp.domain.kid.repository.KidRepository;
import com.erp.domain.kid.repository.ParentKidRepository;
import com.erp.domain.kidapplication.config.KidApplicationWorkflowProperties;
import com.erp.domain.kidapplication.dto.request.AcceptKidApplicationOfferRequest;
import com.erp.domain.kidapplication.dto.request.ApproveKidApplicationRequest;
import com.erp.domain.kidapplication.dto.request.KidApplicationRequest;
import com.erp.domain.kidapplication.dto.request.OfferKidApplicationRequest;
import com.erp.domain.kidapplication.dto.request.RejectRequest;
import com.erp.domain.kidapplication.dto.request.WaitlistKidApplicationRequest;
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
import com.erp.domain.dashboard.service.DashboardService;
import com.erp.global.exception.BusinessException;
import com.erp.global.exception.ErrorCode;
import com.erp.global.security.access.AccessPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KidApplicationService {

    private static final List<ApplicationStatus> ACTIVE_APPLICATION_STATUSES = List.of(
            ApplicationStatus.PENDING,
            ApplicationStatus.WAITLISTED,
            ApplicationStatus.OFFERED
    );

    private static final List<ApplicationStatus> REVIEW_QUEUE_STATUSES = List.of(
            ApplicationStatus.PENDING,
            ApplicationStatus.WAITLISTED,
            ApplicationStatus.OFFERED
    );

    private final KidApplicationRepository applicationRepository;
    private final MemberRepository memberRepository;
    private final KindergartenRepository kindergartenRepository;
    private final KidRepository kidRepository;
    private final ParentKidRepository parentKidRepository;
    private final NotificationService notificationService;
    private final DashboardService dashboardService;
    private final ClassroomCapacityService classroomCapacityService;
    private final KidApplicationWorkflowProperties workflowProperties;
    private final DomainAuditLogService domainAuditLogService;
    private final AccessPolicyService accessPolicyService;

    @Transactional
    public Long apply(KidApplicationRequest request, Long parentId) {
        Member parent = memberRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (parent.getRole() != MemberRole.PARENT) {
            throw new BusinessException(ErrorCode.INVALID_MEMBER_ROLE);
        }

        Kindergarten kindergarten = kindergartenRepository.findById(request.kindergartenId())
                .orElseThrow(() -> new BusinessException(ErrorCode.KINDERGARTEN_NOT_FOUND));

        validateParentKindergartenScope(parent, kindergarten.getId());

        Classroom preferredClassroom = resolvePreferredClassroom(request.preferredClassroomId(), kindergarten.getId());

        applicationRepository.findActiveApplicationByParentAndKindergarten(parentId, request.kindergartenId(), ACTIVE_APPLICATION_STATUSES)
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorCode.APPLICATION_ALREADY_EXISTS);
                });

        if (applicationRepository.existsByParentIdAndStatusInAndDeletedAtIsNull(parentId, ACTIVE_APPLICATION_STATUSES)) {
            throw new BusinessException(ErrorCode.PENDING_APPLICATION_EXISTS);
        }

        KidApplication saved;
        var existing = applicationRepository.findByParentAndKindergarten(parentId, request.kindergartenId());
        if (existing.isPresent()) {
            KidApplication application = existing.get();
            if (application.getStatus().isCancelled() || application.getStatus().isRejected() || application.getStatus().isOfferExpired()) {
                application.reapply(kindergarten, request.kidName(), request.birthDate(), request.gender(), preferredClassroom, request.notes());
                saved = applicationRepository.save(application);
            } else {
                throw new BusinessException(ErrorCode.APPLICATION_ALREADY_EXISTS);
            }
        } else {
            KidApplication application = KidApplication.create(
                    parent,
                    kindergarten,
                    request.kidName(),
                    request.birthDate(),
                    request.gender(),
                    preferredClassroom,
                    request.notes()
            );

            try {
                saved = applicationRepository.save(application);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                throw new BusinessException(ErrorCode.APPLICATION_ALREADY_EXISTS);
            }
        }

        if (parent.getStatus() != MemberStatus.PENDING && parent.getKindergarten() == null) {
            parent.markPending();
        }

        notifyStaffAboutApplication(kindergarten, parent, request.kidName());
        return saved.getId();
    }

    @Transactional
    public void approve(Long applicationId, ApproveKidApplicationRequest request, Long processorId) {
        KidApplication application = getApplicationForUpdate(applicationId);
        if (!application.isPending()) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_PENDING);
        }

        Member processor = getProcessor(processorId, application.getKindergarten().getId());
        Classroom classroom = resolveManagedClassroom(request.classroomId(), application.getKindergarten().getId());
        classroomCapacityService.validateSeatAvailable(classroom);

        Kid savedKid = enrollKid(application, classroom, request.relationshipOrDefault());
        application.approveDirect(classroom, processor, savedKid.getId());

        activateParent(application.getParent(), application.getKindergarten());
        notifyParentAboutApproval(application.getParent(), application.getKidName(), application.getKindergarten());
        domainAuditLogService.record(
                processor,
                application.getKindergarten().getId(),
                DomainAuditAction.KID_APPLICATION_APPROVED,
                DomainAuditTargetType.KID_APPLICATION,
                application.getId(),
                processor.getName() + "이(가) " + application.getKidName() + "의 입학을 승인했습니다.",
                java.util.Map.of(
                        "classroomId", classroom.getId(),
                        "kidId", savedKid.getId()
                )
        );
        dashboardService.evictDashboardStatisticsCache(application.getKindergarten().getId());
    }

    @Transactional
    public void placeOnWaitlist(Long applicationId, WaitlistKidApplicationRequest request, Long processorId) {
        KidApplication application = getApplicationForUpdate(applicationId);
        Member processor = getProcessor(processorId, application.getKindergarten().getId());
        Classroom classroom = resolveManagedClassroom(request.classroomId(), application.getKindergarten().getId());

        application.placeOnWaitlist(classroom, processor, request.decisionNote());
        notifyParentAboutWaitlist(application.getParent(), application.getKidName(), classroom);
        domainAuditLogService.record(
                processor,
                application.getKindergarten().getId(),
                DomainAuditAction.KID_APPLICATION_WAITLISTED,
                DomainAuditTargetType.KID_APPLICATION,
                application.getId(),
                processor.getName() + "이(가) " + application.getKidName() + "을(를) 대기열에 등록했습니다.",
                java.util.Map.of("classroomId", classroom.getId())
        );
    }

    @Transactional
    public void offer(Long applicationId, OfferKidApplicationRequest request, Long processorId) {
        KidApplication application = getApplicationForUpdate(applicationId);
        Member processor = getProcessor(processorId, application.getKindergarten().getId());
        Classroom classroom = resolveManagedClassroom(request.classroomId(), application.getKindergarten().getId());

        classroomCapacityService.validateSeatAvailable(classroom);

        LocalDateTime offerExpiresAt = LocalDateTime.now().plus(workflowProperties.getOfferValidity());
        application.offerSeat(classroom, processor, offerExpiresAt, request.decisionNote());
        notifyParentAboutOffer(application.getParent(), application.getKidName(), classroom, offerExpiresAt);
        domainAuditLogService.record(
                processor,
                application.getKindergarten().getId(),
                DomainAuditAction.KID_APPLICATION_OFFERED,
                DomainAuditTargetType.KID_APPLICATION,
                application.getId(),
                processor.getName() + "이(가) " + application.getKidName() + "에게 입학 제안을 발송했습니다.",
                java.util.Map.of(
                        "classroomId", classroom.getId(),
                        "offerExpiresAt", offerExpiresAt.toString()
                )
        );
    }

    @Transactional
    public void acceptOffer(Long applicationId, AcceptKidApplicationOfferRequest request, Long parentId) {
        KidApplication application = getApplicationForUpdate(applicationId);

        if (!application.getParent().getId().equals(parentId)) {
            throw new BusinessException(ErrorCode.APPLICATION_ACCESS_DENIED);
        }
        if (!application.isOffered()) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_OFFERED);
        }
        if (application.getOfferExpiresAt() != null && !application.getOfferExpiresAt().isAfter(LocalDateTime.now())) {
            application.markOfferExpired();
            notifyParentAboutOfferExpired(application.getParent(), application.getKidName());
            throw new BusinessException(ErrorCode.APPLICATION_OFFER_EXPIRED);
        }

        Classroom classroom = classroomCapacityService.lockClassroom(application.getAssignedClassroom().getId());
        Kid savedKid = enrollKid(application, classroom, request.relationshipOrDefault());
        application.acceptOffer(savedKid.getId());

        activateParent(application.getParent(), application.getKindergarten());
        notifyParentAboutApproval(application.getParent(), application.getKidName(), application.getKindergarten());
        notifyStaffAboutOfferAccepted(classroom.getKindergarten(), application.getParent(), application.getKidName());
        domainAuditLogService.record(
                application.getParent(),
                application.getKindergarten().getId(),
                DomainAuditAction.KID_APPLICATION_OFFER_ACCEPTED,
                DomainAuditTargetType.KID_APPLICATION,
                application.getId(),
                application.getParent().getName() + " 학부모가 " + application.getKidName() + "의 입학 제안을 수락했습니다.",
                java.util.Map.of(
                        "classroomId", classroom.getId(),
                        "kidId", savedKid.getId()
                )
        );
        dashboardService.evictDashboardStatisticsCache(application.getKindergarten().getId());
    }

    @Transactional
    public void reject(Long applicationId, RejectRequest request, Long processorId) {
        KidApplication application = getApplicationForUpdate(applicationId);
        Member processor = getProcessor(processorId, application.getKindergarten().getId());

        application.reject(request.reason(), processor);
        notifyParentAboutRejection(application.getParent(), application.getKidName(), application.getKindergarten(), request.reason());
        domainAuditLogService.record(
                processor,
                application.getKindergarten().getId(),
                DomainAuditAction.KID_APPLICATION_REJECTED,
                DomainAuditTargetType.KID_APPLICATION,
                application.getId(),
                processor.getName() + "이(가) " + application.getKidName() + "의 입학을 거절했습니다.",
                java.util.Map.of("reason", request.reason())
        );
    }

    @Transactional
    public void cancel(Long applicationId, Long parentId) {
        KidApplication application = getApplicationForUpdate(applicationId);

        if (!application.getParent().getId().equals(parentId)) {
            throw new BusinessException(ErrorCode.APPLICATION_ACCESS_DENIED);
        }

        application.cancel();

        Kindergarten kindergarten = application.getKindergarten();
        if (kindergarten != null) {
            notifyStaffAboutCancellation(kindergarten, application.getParent(), application.getKidName());
            domainAuditLogService.record(
                    application.getParent(),
                    kindergarten.getId(),
                    DomainAuditAction.KID_APPLICATION_CANCELLED,
                    DomainAuditTargetType.KID_APPLICATION,
                    application.getId(),
                    application.getParent().getName() + " 학부모가 " + application.getKidName() + "의 입학 신청을 취소했습니다.",
                    java.util.Map.of("status", application.getStatus().name())
            );
        }
    }

    @Transactional(readOnly = true)
    public List<KidApplicationResponse> getPendingApplications(Long kindergartenId, Long memberId) {
        Member member = getStaffReviewer(memberId, kindergartenId);
        List<KidApplication> applications = applicationRepository.findPendingApplicationsByKindergartenId(kindergartenId);
        return applications.stream()
                .map(KidApplicationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<KidApplicationResponse> getReviewQueueApplications(Long kindergartenId, Long memberId) {
        getStaffReviewer(memberId, kindergartenId);
        return applicationRepository.findReviewQueueByKindergartenId(kindergartenId, REVIEW_QUEUE_STATUSES)
                .stream()
                .map(KidApplicationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<KidApplicationResponse> getMyApplications(Long parentId) {
        return applicationRepository.findByParentIdAndDeletedAtIsNullOrderByCreatedAtDesc(parentId)
                .stream()
                .map(KidApplicationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public KidApplicationResponse getApplication(Long applicationId, Long memberId) {
        KidApplication application = applicationRepository.findByIdAndDeletedAtIsNull(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        Member member = accessPolicyService.getRequester(memberId);
        accessPolicyService.validateKidApplicationReadAccess(member, application);

        return KidApplicationResponse.from(application);
    }

    @Transactional
    @Scheduled(fixedDelayString = "${app.kid-application.expire-offers-fixed-delay-ms:60000}")
    public void expireOffers() {
        if (!workflowProperties.isExpireOffersEnabled()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<KidApplication> expiredOffers = applicationRepository.findExpiredOffers(ApplicationStatus.OFFERED, now);
        for (KidApplication application : expiredOffers) {
            if (!application.isOffered()) {
                continue;
            }
            application.markOfferExpired();
            notifyParentAboutOfferExpired(application.getParent(), application.getKidName());
            domainAuditLogService.recordSystem(
                    application.getKindergarten().getId(),
                    DomainAuditAction.KID_APPLICATION_OFFER_EXPIRED,
                    DomainAuditTargetType.KID_APPLICATION,
                    application.getId(),
                    application.getKidName() + "의 입학 제안이 만료되었습니다.",
                    java.util.Map.of("offerExpiresAt", now.toString())
            );
        }
    }

    private Member getStaffReviewer(Long memberId, Long kindergartenId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getKindergarten() == null || !member.getKindergarten().getId().equals(kindergartenId)) {
            throw new BusinessException(ErrorCode.KINDERGARTEN_ACCESS_DENIED);
        }

        if (member.getRole() != MemberRole.PRINCIPAL && member.getRole() != MemberRole.TEACHER) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return member;
    }

    private KidApplication getApplicationForUpdate(Long applicationId) {
        return applicationRepository.findByIdAndDeletedAtIsNullForUpdate(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));
    }

    private Member getProcessor(Long processorId, Long kindergartenId) {
        return getStaffReviewer(processorId, kindergartenId);
    }

    private Classroom resolveManagedClassroom(Long classroomId, Long kindergartenId) {
        Classroom classroom = classroomCapacityService.lockClassroom(classroomId);
        if (!classroom.getKindergarten().getId().equals(kindergartenId)) {
            throw new BusinessException(ErrorCode.CLASSROOM_NOT_BELONG_TO_KINDERGARTEN);
        }
        return classroom;
    }

    private Classroom resolvePreferredClassroom(Long classroomId, Long kindergartenId) {
        if (classroomId == null) {
            return null;
        }
        Classroom classroom = classroomCapacityService.lockClassroom(classroomId);
        if (!classroom.getKindergarten().getId().equals(kindergartenId)) {
            throw new BusinessException(ErrorCode.CLASSROOM_NOT_BELONG_TO_KINDERGARTEN);
        }
        return classroom;
    }

    private Kid enrollKid(KidApplication application, Classroom classroom, com.erp.domain.kid.entity.Relationship relationship) {
        Kid kid = Kid.create(
                classroom,
                application.getKidName(),
                application.getBirthDate(),
                application.getGender(),
                LocalDate.now()
        );
        Kid savedKid = kidRepository.save(kid);

        ParentKid parentKid = ParentKid.create(savedKid, application.getParent(), relationship);
        parentKidRepository.save(parentKid);
        return savedKid;
    }

    private void activateParent(Member parent, Kindergarten kindergarten) {
        if (parent.getKindergarten() == null) {
            parent.assignKindergarten(kindergarten);
        }
        parent.activateMember();
    }

    private void validateParentKindergartenScope(Member parent, Long requestedKindergartenId) {
        if (parent.getKindergarten() == null) {
            return;
        }
        if (!parent.getKindergarten().getId().equals(requestedKindergartenId)) {
            throw new BusinessException(ErrorCode.ALREADY_ASSIGNED_TO_KINDERGARTEN);
        }
    }

    private void notifyStaffAboutApplication(Kindergarten kindergarten, Member parent, String kidName) {
        String content = parent.getName() + " 학부모님이 자녀(" + kidName + ")의 입학을 신청했습니다.";

        memberRepository.findByKindergartenIdAndRole(kindergarten.getId(), MemberRole.PRINCIPAL)
                .ifPresent(principal -> notificationService.notifyWithLink(
                        principal.getId(),
                        NotificationType.KID_APPLICATION_SUBMITTED,
                        "새로운 입학 신청",
                        content,
                        "/applications/pending"
                ));

        memberRepository.findAllByKindergartenIdAndRole(kindergarten.getId(), MemberRole.TEACHER)
                .forEach(teacher -> notificationService.notifyWithLink(
                        teacher.getId(),
                        NotificationType.KID_APPLICATION_SUBMITTED,
                        "새로운 입학 신청",
                        content,
                        "/applications/pending"
                ));
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

    private void notifyStaffAboutOfferAccepted(Kindergarten kindergarten, Member parent, String kidName) {
        String content = parent.getName() + " 학부모님이 " + kidName + "의 입학 제안을 수락했습니다.";
        memberRepository.findByKindergartenIdAndRole(kindergarten.getId(), MemberRole.PRINCIPAL)
                .ifPresent(principal -> notificationService.notifyWithLink(
                        principal.getId(),
                        NotificationType.KID_APPLICATION_OFFER_ACCEPTED,
                        "입학 제안 수락",
                        content,
                        "/applications/pending"
                ));
        memberRepository.findAllByKindergartenIdAndRole(kindergarten.getId(), MemberRole.TEACHER)
                .forEach(teacher -> notificationService.notifyWithLink(
                        teacher.getId(),
                        NotificationType.KID_APPLICATION_OFFER_ACCEPTED,
                        "입학 제안 수락",
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

    private void notifyParentAboutWaitlist(Member parent, String kidName, Classroom classroom) {
        notificationService.notifyWithLink(
                parent.getId(),
                NotificationType.KID_APPLICATION_WAITLISTED,
                "입학 대기열 등록",
                kidName + "이(가) " + classroom.getName() + " 대기열에 등록되었습니다.",
                "/applications/pending"
        );
    }

    private void notifyParentAboutOffer(Member parent, String kidName, Classroom classroom, LocalDateTime offerExpiresAt) {
        String content = kidName + "에게 " + classroom.getName() + " 입학 제안이 도착했습니다. "
                + "만료 시각: " + offerExpiresAt;
        notificationService.notifyWithLink(
                parent.getId(),
                NotificationType.KID_APPLICATION_OFFERED,
                "입학 제안 도착",
                content,
                "/applications/pending"
        );
    }

    private void notifyParentAboutOfferExpired(Member parent, String kidName) {
        notificationService.notifyWithLink(
                parent.getId(),
                NotificationType.KID_APPLICATION_OFFER_EXPIRED,
                "입학 제안 만료",
                kidName + "의 입학 제안이 만료되었습니다.",
                "/applications/pending"
        );
    }
}
