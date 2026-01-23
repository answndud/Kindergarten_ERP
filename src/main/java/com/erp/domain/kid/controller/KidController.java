package com.erp.domain.kid.controller;

import com.erp.domain.kid.dto.request.AssignParentRequest;
import com.erp.domain.kid.dto.request.KidRequest;
import com.erp.domain.kid.dto.request.UpdateClassroomRequest;
import com.erp.domain.kid.dto.response.ClassroomKidCountResponse;
import com.erp.domain.kid.dto.response.KidDetailResponse;
import com.erp.domain.kid.dto.response.KidResponse;
import com.erp.domain.kid.entity.Kid;
import com.erp.domain.kid.service.KidService;
import com.erp.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.erp.global.security.user.CustomUserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 원생 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/kids")
@RequiredArgsConstructor
public class KidController {

    private final KidService kidService;

    /**
     * 원생 생성 (원장, 교사만 가능)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<KidResponse>> create(
            @Valid @RequestBody KidRequest request) {

        Long id = kidService.createKid(request);

        Kid kid = kidService.getKid(id);

        return ResponseEntity
                .ok(ApiResponse.success(KidResponse.from(kid), "원생이 등록되었습니다"));
    }

    /**
     * 원생 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KidDetailResponse>> getKid(@PathVariable Long id) {
        KidDetailResponse response = kidService.getKidDetail(id);

        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

    /**
     * 반별 원생 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<KidResponse>>> getKids(
            @RequestParam(required = false) Long classroomId,
            @RequestParam(required = false) Long kindergartenId,
            @RequestParam(required = false) String name) {

        List<Kid> kids;

        if (classroomId != null) {
            if (name != null && !name.isBlank()) {
                kids = kidService.searchKidsByName(classroomId, name);
            } else {
                kids = kidService.getKidsByClassroom(classroomId);
            }
        } else if (kindergartenId != null) {
            if (name != null && !name.isBlank()) {
                kids = kidService.searchKidsByKindergarten(kindergartenId, name);
            } else {
                kids = kidService.getKidsByKindergarten(kindergartenId);
            }
        } else {
            // 전체 조회는 추후 구현
            kids = List.of();
        }

        List<KidResponse> responses = kids.stream()
                .map(KidResponse::from)
                .toList();

        return ResponseEntity
                .ok(ApiResponse.success(responses));
    }

    /**
     * 반별 원생 수 조회
     */
    @GetMapping("/classroom-counts")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<ClassroomKidCountResponse>>> getClassroomCounts(
            @RequestParam Long kindergartenId) {
        java.util.Map<Long, Long> counts = kidService.getClassroomCounts(kindergartenId);
        List<ClassroomKidCountResponse> responses = counts.entrySet().stream()
                .map(entry -> ClassroomKidCountResponse.of(entry.getKey(), entry.getValue()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 원생 목록 조회 (페이지)
     */
    @GetMapping("/page")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<KidResponse>>> getKidsPage(
            @RequestParam(required = false) Long classroomId,
            @RequestParam(required = false) Long kindergartenId,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "name") String sort) {

        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                safePage,
                safeSize,
                resolveSort(sort)
        );

        org.springframework.data.domain.Page<Kid> kidsPage;

        if (classroomId != null) {
            if (name != null && !name.isBlank()) {
                kidsPage = kidService.searchKidsByName(classroomId, name, pageable);
            } else {
                kidsPage = kidService.getKidsByClassroom(classroomId, pageable);
            }
        } else if (kindergartenId != null) {
            if (name != null && !name.isBlank()) {
                kidsPage = kidService.searchKidsByKindergarten(kindergartenId, name, pageable);
            } else {
                kidsPage = kidService.getKidsByKindergarten(kindergartenId, pageable);
            }
        } else {
            kidsPage = org.springframework.data.domain.Page.empty(pageable);
        }

        org.springframework.data.domain.Page<KidResponse> responses = kidsPage.map(KidResponse::from);

        return ResponseEntity
                .ok(ApiResponse.success(responses));
    }

    private org.springframework.data.domain.Sort resolveSort(String sortKey) {
        if ("recent".equalsIgnoreCase(sortKey)) {
            return org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
        }
        if ("age".equalsIgnoreCase(sortKey)) {
            return org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "birthDate");
        }
        return org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "name");
    }

    /**
     * 학부모의 원생 목록 조회
     */
    @GetMapping("/my-kids")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ApiResponse<List<KidResponse>>> getMyKids(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long parentId = userDetails.getMemberId();

        List<Kid> kids = kidService.getKidsByParent(parentId);

        List<KidResponse> responses = kids.stream()
                .map(KidResponse::from)
                .toList();

        return ResponseEntity
                .ok(ApiResponse.success(responses));
    }


    /**
     * 원생 수정 (원장, 교사만 가능)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<KidResponse>> updateKid(
            @PathVariable Long id,
            @Valid @RequestBody KidRequest request) {

        kidService.updateKid(id, request.getName(), request.getBirthDate(), request.getGender());

        Kid kid = kidService.getKid(id);

        return ResponseEntity
                .ok(ApiResponse.success(KidResponse.from(kid), "원생 정보가 수정되었습니다"));
    }

    /**
     * 반 배정 변경 (원장만 가능)
     */
    @PutMapping("/{id}/classroom")
    @PreAuthorize("hasRole('PRINCIPAL')")
    public ResponseEntity<ApiResponse<KidResponse>> updateClassroom(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClassroomRequest request) {

        kidService.updateClassroom(id, request);

        Kid kid = kidService.getKid(id);

        return ResponseEntity
                .ok(ApiResponse.success(KidResponse.from(kid), "반이 변경되었습니다"));
    }

    /**
     * 학부모 연결 (원장만 가능)
     */
    @PostMapping("/{id}/parents")
    @PreAuthorize("hasRole('PRINCIPAL')")
    public ResponseEntity<ApiResponse<KidDetailResponse>> assignParent(
            @PathVariable Long id,
            @Valid @RequestBody AssignParentRequest request) {

        kidService.assignParent(id, request);

        KidDetailResponse response = kidService.getKidDetail(id);

        return ResponseEntity
                .ok(ApiResponse.success(response, "학부모가 연결되었습니다"));
    }

    /**
     * 학부모 연결 해제 (원장만 가능)
     */
    @DeleteMapping("/{id}/parents/{parentId}")
    @PreAuthorize("hasRole('PRINCIPAL')")
    public ResponseEntity<ApiResponse<KidDetailResponse>> removeParent(
            @PathVariable Long id,
            @PathVariable Long parentId) {

        kidService.removeParent(id, parentId);

        KidDetailResponse response = kidService.getKidDetail(id);

        return ResponseEntity
                .ok(ApiResponse.success(response, "학부모 연결이 해제되었습니다"));
    }

    /**
     * 원생 삭제 (원장만 가능)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRINCIPAL')")
    public ResponseEntity<ApiResponse<Void>> deleteKid(@PathVariable Long id) {
        kidService.deleteKid(id);

        return ResponseEntity
                .ok(ApiResponse.success(null, "원생이 삭제되었습니다"));
    }
}
