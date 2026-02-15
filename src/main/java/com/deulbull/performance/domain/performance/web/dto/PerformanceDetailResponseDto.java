package com.deulbull.performance.domain.performance.web.dto;

import java.util.List;

public record PerformanceDetailResponseDto(
        Long performanceId,
        List<String> imageUrls,
        String title,
        String dateTime,
        String venue,
        String phoneNumber,
        List<String> posterUrls,
        String location
) {
}
