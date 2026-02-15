package com.deulbull.performance.domain.performance.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 공연 생성 시 기본 정보 (JSON part: basicInfo)
 */
public record PerformanceCreateBasicDto(
        @NotBlank(message = "공연 제목은 필수 입력 항목입니다.")
        String title,

        @NotBlank(message = "주소는 필수 입력 항목입니다.")
        String location,

        @NotBlank(message = "공연장 이름은 필수 입력 항목입니다.")
        String venue,

        @NotNull(message = "공연 날짜와 시간은 필수 입력 항목입니다.")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime dateTime,

        String phoneNumber,

        Integer preSaleFee,
        Integer onSiteFee,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime preSaleEndTime,

        String bankName,
        String bankAccount,
        String accountHolder,
        String kakaopayUrl,
        String naverpayUrl,
        String setlistUrl
) {
}
