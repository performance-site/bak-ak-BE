package com.deulbull.performance.domain.performance.entity;

import com.deulbull.performance.domain.performanceSongs.entity.PerformanceSong;
import com.deulbull.performance.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Performance extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String websiteName; // 웹사이트 이름
    private String websiteDescription; // 웹사이트 설명
    private String title;
    private String subtitle;
    private String description;
    private String location; // 주소 (ex. 서울 서대문구 연세로5다길 10 지하1층)
    private String venue; // 공연장 이름 (ex. 신촌 몽향)
    private LocalDateTime dateTime;
    private Integer preSaleFee; // 사전예매 가격
    private Integer onSiteFee; // 현장예매 가격
    private LocalDateTime preSaleEndTime; // 사전 예매 종료 시간
    private String posterFrontUrl; // 포스터 앞면 이미지 URL
    private String posterBackUrl;  // 포스터 뒷면 이미지 URL
    private String openchatUrl;
    private String phoneNumber; // 연락처 (010-xxxx-xxxx), null 가능
    private String preSaleFormUrl; // 사전예매 폼 URL, null 가능
    private String bankName; // 은행명
    private String bankAccount; // 계좌번호
    private String accountHolder; // 예금주
    private String kakaopayUrl; // 카카오페이 결제 URL
    private String naverpayUrl; // 네이버페이 결제 URL
    private String setlistUrl; // 셋리스트 이미지 또는 파일 URL

    @ManyToOne
    @JoinColumn(name = "current_song_id")
    private PerformanceSong currentSong; // 현재 진행중인 곡
}
