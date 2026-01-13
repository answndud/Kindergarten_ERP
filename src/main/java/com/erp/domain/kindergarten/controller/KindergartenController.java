package com.erp.domain.kindergarten.controller;

import com.erp.domain.kindergarten.dto.request.KindergartenRequest;
import com.erp.domain.kindergarten.dto.response.KindergartenResponse;
import com.erp.domain.kindergarten.entity.Kindergarten;
import com.erp.domain.kindergarten.service.KindergartenService;
import com.erp.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 유치원 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/kindergartens")
@RequiredArgsConstructor
public class KindergartenController {

    private final KindergartenService kindergartenService;

    /**
     * 유치원 등록 (원장만 가능)
     */
    @PostMapping
    @PreAuthorize("hasRole('PRINCIPAL')")
    public ResponseEntity<ApiResponse<KindergartenResponse>> create(
            @Valid @RequestBody KindergartenRequest request) {
        Long id = kindergartenService.register(
                request.getName(),
                request.getAddress(),
                request.getPhone(),
                request.getOpenTime(),
                request.getCloseTime()
        );

        Kindergarten kindergarten = kindergartenService.getKindergarten(id);

        return ResponseEntity
                .ok(ApiResponse.success(KindergartenResponse.from(kindergarten), "유치원이 등록되었습니다"));
    }

    /**
     * 유치원 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KindergartenResponse>> getKindergarten(@PathVariable Long id) {
        Kindergarten kindergarten = kindergartenService.getKindergarten(id);

        return ResponseEntity
                .ok(ApiResponse.success(KindergartenResponse.from(kindergarten)));
    }

    /**
     * 전체 유치원 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<KindergartenResponse>>> getAllKindergartens() {
        List<Kindergarten> kindergartens = kindergartenService.getAllKindergartens();

        List<KindergartenResponse> responses = kindergartens.stream()
                .map(KindergartenResponse::from)
                .toList();

        return ResponseEntity
                .ok(ApiResponse.success(responses));
    }

    /**
     * 유치원 수정 (원장만 가능)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRINCIPAL')")
    public ResponseEntity<ApiResponse<KindergartenResponse>> updateKindergarten(
            @PathVariable Long id,
            @Valid @RequestBody KindergartenRequest request) {

        kindergartenService.updateKindergarten(
                id,
                request.getName(),
                request.getAddress(),
                request.getPhone(),
                request.getOpenTime(),
                request.getCloseTime()
        );

        Kindergarten kindergarten = kindergartenService.getKindergarten(id);

        return ResponseEntity
                .ok(ApiResponse.success(KindergartenResponse.from(kindergarten), "유치원 정보가 수정되었습니다"));
    }

    /**
     * 유치원 삭제 (원장만 가능)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRINCIPAL')")
    public ResponseEntity<ApiResponse<Void>> deleteKindergarten(@PathVariable Long id) {
        kindergartenService.deleteKindergarten(id);

        return ResponseEntity
                .ok(ApiResponse.success(null, "유치원이 삭제되었습니다"));
    }
}
