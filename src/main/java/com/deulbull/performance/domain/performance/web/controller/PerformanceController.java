package com.deulbull.performance.domain.performance.web.controller;

import com.deulbull.performance.domain.performance.service.PerformanceService;
import com.deulbull.performance.domain.performance.web.dto.PerformanceCreateBasicDto;
import com.deulbull.performance.domain.performance.web.dto.PerformanceDetailResponseDto;
import com.deulbull.performance.domain.performance.web.dto.PerformanceSetlistRequestDto;
import com.deulbull.performance.domain.performance.web.dto.PerformanceSetlistResponse;
import com.deulbull.performance.global.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/performances")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;

    // 공연 생성 API (basicInfo, setlist 각각 별도 part로 전달)
    @PostMapping(consumes = {"multipart/form-data"})
    public SuccessResponse<PerformanceDetailResponseDto> createPerformance(
            @Valid @RequestPart("basicInfo") PerformanceCreateBasicDto basicInfo,
            @Valid @RequestPart("setlist") PerformanceSetlistRequestDto setlistRequest,
            @RequestPart("posterFront") MultipartFile posterFront,
            @RequestPart(value = "posterBack", required = false) MultipartFile posterBack,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        PerformanceDetailResponseDto response = performanceService.createPerformance(basicInfo, setlistRequest, posterFront, posterBack, images);
        return SuccessResponse.created(response);
    }

    // 공연 상세 조회
    @GetMapping("/{performanceId}")
    public SuccessResponse<PerformanceDetailResponseDto> getPerformanceDetail(
            @PathVariable Long performanceId
    ) {
        PerformanceDetailResponseDto response = performanceService.getPerformanceDetail(performanceId);
        return SuccessResponse.ok(response);
    }

    // 공연 셋리스트 조회
    @GetMapping("/{performanceId}/setlist")
    public SuccessResponse<PerformanceSetlistResponse> getPerformanceSetList(
            @PathVariable Long performanceId
    ){
        PerformanceSetlistResponse response = performanceService.getPerformanceSetlist(performanceId);
        return SuccessResponse.ok(response);
    }

    // 공연 이미지 교체
    @PutMapping(value = "/{performanceId}/images", consumes = {"multipart/form-data"})
    public SuccessResponse<Void> replacePerformanceImages(
            @PathVariable Long performanceId,
            @RequestPart("images") List<MultipartFile> images
    ) {
        performanceService.replacePerformanceImages(performanceId, images);
        return SuccessResponse.ok(null);
    }
}
