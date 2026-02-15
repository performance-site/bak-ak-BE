package com.deulbull.performance.domain.performance.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * 공연 생성 시 셋리스트 (JSON part: setlist)
 */
public record PerformanceSetlistRequestDto(
        @Valid
        @NotNull(message = "셋리스트는 필수 입력 항목입니다.")
        List<PerformanceSongCreateDto> setlist
) {
    /** 공연별 곡 정보 */
    public record PerformanceSongCreateDto(
            @NotNull(message = "곡 순서는 필수 입력 항목입니다.")
            Integer orderInPerformance,

            @Valid
            @NotNull(message = "곡 정보는 필수 입력 항목입니다.")
            SongCreateDto song,

            MembersDto members
    ) {}

    /** 곡별 멤버 정보 */
    public record MembersDto(
            List<String> vocal,
            List<String> guitar1,
            List<String> guitar2,
            List<String> bass,
            List<String> drum,
            List<String> keyboard
    ) {}

    /** 곡 정보 */
    public record SongCreateDto(
            @NotBlank(message = "곡 제목은 필수 입력 항목입니다.")
            String title,

            @NotBlank(message = "아티스트는 필수 입력 항목입니다.")
            String artist,

            String album,

            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate releaseDate,

            String genre,
            String youtubeUrl,
            String albumImgUrl,
            String lyrics
    ) {}
}
