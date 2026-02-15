package com.deulbull.performance.domain.performance.service;

import com.deulbull.performance.domain.performance.web.dto.PerformanceCreateBasicDto;
import com.deulbull.performance.domain.performance.web.dto.PerformanceDetailResponseDto;
import com.deulbull.performance.domain.performance.web.dto.PerformanceSetlistRequestDto;
import com.deulbull.performance.domain.performance.web.dto.PerformanceSetlistResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PerformanceService {
    // 공연 생성
    PerformanceDetailResponseDto createPerformance(
            PerformanceCreateBasicDto basicInfo,
            PerformanceSetlistRequestDto setlistRequest,
            MultipartFile posterFront,
            MultipartFile posterBack,
            List<MultipartFile> images);

    // 공연 정보 상세 조회
    PerformanceDetailResponseDto getPerformanceDetail(Long performanceId);

    // 공연 셋리스트 조회
    PerformanceSetlistResponse getPerformanceSetlist(Long performanceId);

    // 공연 이미지 교체
    void replacePerformanceImages(Long performanceId, List<MultipartFile> newImages);
}
