package com.erp.domain.announcement.controller;

import com.erp.domain.announcement.dto.request.AnnouncementRequest;
import com.erp.domain.announcement.dto.response.AnnouncementResponse;
import com.erp.domain.announcement.entity.Announcement;
import com.erp.domain.announcement.service.AnnouncementService;
import com.erp.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 공지사항 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 공지사항 생성 (원장, 교사만 가능)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> create(
            @Valid @RequestBody AnnouncementRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 임시로 writerId를 1로 설정 (추후 SecurityContext에서 실제 사용자 ID 가져오기)
        Long writerId = 1L;

        Long id = announcementService.createAnnouncement(request, writerId);

        Announcement announcement = announcementService.getAnnouncementWithoutIncrement(id);

        return ResponseEntity
                .ok(ApiResponse.success(announcementService.toResponse(announcement), "공지사항이 작성되었습니다"));
    }

    /**
     * 공지사항 조회 (조회수 증가)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> getAnnouncement(@PathVariable Long id) {
        Announcement announcement = announcementService.getAnnouncement(id);

        return ResponseEntity
                .ok(ApiResponse.success(announcementService.toResponse(announcement)));
    }

    /**
     * 유치원별 공지사항 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AnnouncementResponse>>> getAnnouncements(
            @RequestParam Long kindergartenId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Announcement> announcements = announcementService.getAnnouncementsByKindergarten(kindergartenId, page, size);

        Page<AnnouncementResponse> responses = announcements.map(announcementService::toResponse);

        return ResponseEntity
                .ok(ApiResponse.success(responses));
    }

    /**
     * 중요 공지사항 목록 조회
     */
    @GetMapping("/important")
    public ResponseEntity<ApiResponse<Page<AnnouncementResponse>>> getImportantAnnouncements(
            @RequestParam Long kindergartenId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Announcement> announcements = announcementService.getImportantAnnouncements(kindergartenId, page, size);

        Page<AnnouncementResponse> responses = announcements.map(announcementService::toResponse);

        return ResponseEntity
                .ok(ApiResponse.success(responses));
    }

    /**
     * 공지사항 검색 (제목으로)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<AnnouncementResponse>>> searchAnnouncements(
            @RequestParam Long kindergartenId,
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Announcement> announcements = announcementService.searchByTitle(kindergartenId, title, page, size);

        Page<AnnouncementResponse> responses = announcements.map(announcementService::toResponse);

        return ResponseEntity
                .ok(ApiResponse.success(responses));
    }

    /**
     * 인기 공지사항 (조회수 순)
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<Page<AnnouncementResponse>>> getMostViewedAnnouncements(
            @RequestParam Long kindergartenId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Announcement> announcements = announcementService.getMostViewedAnnouncements(kindergartenId, page, size);

        Page<AnnouncementResponse> responses = announcements.map(announcementService::toResponse);

        return ResponseEntity
                .ok(ApiResponse.success(responses));
    }

    /**
     * 공지사항 수정 (작성자만 가능)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 임시로 writerId를 1로 설정
        Long writerId = 1L;

        announcementService.updateAnnouncement(id, request, writerId);

        Announcement announcement = announcementService.getAnnouncementWithoutIncrement(id);

        return ResponseEntity
                .ok(ApiResponse.success(announcementService.toResponse(announcement), "공지사항이 수정되었습니다"));
    }

    /**
     * 공지사항 삭제 (작성자 또는 원장만 가능)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 임시로 requesterId를 1로 설정
        Long requesterId = 1L;

        announcementService.deleteAnnouncement(id, requesterId);

        return ResponseEntity
                .ok(ApiResponse.success(null, "공지사항이 삭제되었습니다"));
    }

    /**
     * 중요 공지 토글 (작성자 또는 원장만 가능)
     */
    @PatchMapping("/{id}/important")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> toggleImportant(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 임시로 requesterId를 1로 설정
        Long requesterId = 1L;

        announcementService.toggleImportant(id, requesterId);

        Announcement announcement = announcementService.getAnnouncementWithoutIncrement(id);

        return ResponseEntity
                .ok(ApiResponse.success(announcementService.toResponse(announcement), "중요 공지 설정이 변경되었습니다"));
    }
}
