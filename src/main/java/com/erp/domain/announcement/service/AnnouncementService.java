package com.erp.domain.announcement.service;

import com.erp.domain.announcement.dto.request.AnnouncementRequest;
import com.erp.domain.announcement.dto.response.AnnouncementResponse;
import com.erp.domain.announcement.entity.Announcement;
import com.erp.domain.announcement.repository.AnnouncementRepository;
import com.erp.domain.dashboard.service.DashboardService;
import com.erp.domain.kindergarten.service.KindergartenService;
import com.erp.domain.member.entity.Member;
import com.erp.domain.member.entity.MemberRole;
import com.erp.domain.member.service.MemberService;
import com.erp.domain.notification.entity.NotificationType;
import com.erp.domain.notification.service.NotificationService;
import com.erp.global.exception.BusinessException;
import com.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 공지사항 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final KindergartenService kindergartenService;
    private final MemberService memberService;
    private final NotificationService notificationService;
    private final DashboardService dashboardService;

    /**
     * 공지사항 생성
     */
    @Transactional
    public Long createAnnouncement(AnnouncementRequest request, Long writerId) {
        // 유치원 조회
        var kindergarten = kindergartenService.getKindergarten(request.getKindergartenId());
        // 작성자 조회
        Member writer = memberService.getMemberById(writerId);

        // 작성자 역할 확인 (원장 또는 교사)
        validateWriterRole(writer);

        // 공지사항 생성
        Announcement announcement;
        if (Boolean.TRUE.equals(request.getIsImportant())) {
            announcement = Announcement.createImportant(kindergarten, writer, request.getTitle(), request.getContent());
        } else {
            announcement = Announcement.create(kindergarten, writer, request.getTitle(), request.getContent());
        }

        Announcement saved = announcementRepository.save(announcement);
        evictDashboardStatistics(saved);

        List<MemberRole> targetRoles = resolveTargetRoles(request.getTargetRoles());
        List<Member> receivers = memberService.getMembersByKindergartenAndRoles(
                kindergarten.getId(),
                targetRoles
        );
        if (!receivers.isEmpty()) {
            String title = "새 공지사항: " + saved.getTitle();
            String content = saved.getContent();
            List<Long> receiverIds = new java.util.ArrayList<>();
            for (Member receiver : receivers) {
                receiverIds.add(receiver.getId());
            }
            notificationService.notifyWithLink(receiverIds,
                    NotificationType.ANNOUNCEMENT_CREATED,
                    title,
                    content,
                    "/announcements"
            );
        }

        return saved.getId();
    }

    /**
     * 공지사항 조회 (조회수 증가)
     */
    @Transactional
    public Announcement getAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

        // 조회수 증가
        announcement.incrementViewCount();
        evictDashboardStatistics(announcement);

        return announcement;
    }

    /**
     * 공지사항 조회 (조회수 증가 없음)
     */
    public Announcement getAnnouncementWithoutIncrement(Long id) {
        return announcementRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));
    }

    /**
     * 유치원별 공지사항 목록 조회 (뷰용 - 연관 엔티티 포함)
     */
    public java.util.List<Announcement> getAnnouncementsByKindergartenForView(Long kindergartenId) {
        // 유치원 존재 확인
        kindergartenService.getKindergarten(kindergartenId);
        return announcementRepository.findByKindergartenIdWithRelations(kindergartenId);
    }

    /**
     * 유치원별 공지사항 목록 조회
     */
    public Page<Announcement> getAnnouncementsByKindergarten(Long kindergartenId, int page, int size) {
        // 유치원 존재 확인
        kindergartenService.getKindergarten(kindergartenId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("isImportant").descending()
                .and(Sort.by("createdAt").descending()));

        return announcementRepository.findByKindergartenIdAndDeletedAtIsNull(kindergartenId, pageable);
    }

    /**
     * 유치원별 중요 공지사항 목록 조회
     */
    public Page<Announcement> getImportantAnnouncements(Long kindergartenId, int page, int size) {
        // 유치원 존재 확인
        kindergartenService.getKindergarten(kindergartenId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return announcementRepository.findImportantByKindergartenId(kindergartenId, pageable);
    }

    /**
     * 제목으로 검색
     */
    public Page<Announcement> searchByTitle(Long kindergartenId, String title, int page, int size) {
        // 유치원 존재 확인
        kindergartenService.getKindergarten(kindergartenId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return announcementRepository.findByKindergartenIdAndTitleContaining(kindergartenId, title, pageable);
    }

    /**
     * 인기 공지사항 (조회수 순)
     */
    public Page<Announcement> getMostViewedAnnouncements(Long kindergartenId, int page, int size) {
        // 유치원 존재 확인
        kindergartenService.getKindergarten(kindergartenId);

        Pageable pageable = PageRequest.of(page, size);

        return announcementRepository.findMostViewedByKindergartenId(kindergartenId, pageable);
    }

    /**
     * 공지사항 수정
     */
    @Transactional
    public void updateAnnouncement(Long id, AnnouncementRequest request, Long requesterId) {
        Announcement announcement = getAnnouncementWithoutIncrement(id);

        // 수정 권한 확인 (원장 또는 교사만 가능)
        Member requester = memberService.getMemberById(requesterId);
        validateWriterRole(requester);

        announcement.update(request.getTitle(), request.getContent());

        // 중요 공지 설정 변경
        if (request.getIsImportant() != null) {
            announcement.setImportant(request.getIsImportant());
        }
        evictDashboardStatistics(announcement);
    }

    /**
     * 공지사항 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteAnnouncement(Long id, Long requesterId) {
        Announcement announcement = getAnnouncementWithoutIncrement(id);

        // 삭제 권한 확인 (원장 또는 교사만 가능)
        Member requester = memberService.getMemberById(requesterId);
        validateWriterRole(requester);

        announcement.softDelete();
        evictDashboardStatistics(announcement);
    }

    /**
     * 중요 공지 토글
     */
    @Transactional
    public void toggleImportant(Long id, Long requesterId) {
        Announcement announcement = getAnnouncementWithoutIncrement(id);

        // 토글 권한 확인 (원장 또는 교사만 가능)
        Member requester = memberService.getMemberById(requesterId);
        validateWriterRole(requester);

        announcement.toggleImportant();
        evictDashboardStatistics(announcement);
    }

    /**
     * 작성자 역할 확인 (원장 또는 교사)
     */
    private void validateWriterRole(Member writer) {
        if (writer.getRole() != com.erp.domain.member.entity.MemberRole.TEACHER &&
            writer.getRole() != com.erp.domain.member.entity.MemberRole.PRINCIPAL) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    /**
     * Announcement Response 변환
     */
    public AnnouncementResponse toResponse(Announcement announcement) {
        return AnnouncementResponse.from(announcement);
    }

    private List<MemberRole> resolveTargetRoles(List<MemberRole> targetRoles) {
        if (targetRoles == null || targetRoles.isEmpty()) {
            return List.of(MemberRole.PRINCIPAL, MemberRole.TEACHER, MemberRole.PARENT);
        }
        return targetRoles;
    }

    private void evictDashboardStatistics(Announcement announcement) {
        dashboardService.evictDashboardStatisticsCache(announcement.getKindergarten().getId());
    }
}
