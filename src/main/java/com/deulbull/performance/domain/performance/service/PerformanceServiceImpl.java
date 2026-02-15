package com.deulbull.performance.domain.performance.service;

import com.deulbull.performance.domain.band.entity.BandSession;
import com.deulbull.performance.domain.band.entity.Person;
import com.deulbull.performance.domain.band.entity.enums.SessionType;
import com.deulbull.performance.domain.band.repository.BandSessionRepository;
import com.deulbull.performance.domain.band.repository.PersonRepository;
import com.deulbull.performance.domain.performance.entity.Performance;
import com.deulbull.performance.domain.performance.entity.PerformanceImage;
import com.deulbull.performance.domain.performance.exception.PerformanceNotFoundException;
import com.deulbull.performance.domain.performance.repository.PerformanceImageRepository;
import com.deulbull.performance.domain.performance.repository.PerformanceRepository;
import com.deulbull.performance.domain.performance.web.dto.PerformanceCreateBasicDto;
import com.deulbull.performance.domain.performance.web.dto.PerformanceDetailResponseDto;
import com.deulbull.performance.domain.performance.web.dto.PerformanceSetlistRequestDto;
import com.deulbull.performance.domain.performance.web.dto.PerformanceSetlistResponse;
import com.deulbull.performance.domain.performance.web.dto.PerformanceSetlistResponse.PerformanceSetListDetail;
import com.deulbull.performance.domain.performanceSongs.entity.PerformanceSong;
import com.deulbull.performance.domain.performanceSongs.repository.PerformanceSongsRepository;
import com.deulbull.performance.domain.song.entity.Song;
import com.deulbull.performance.domain.song.repository.SongRepository;
import com.deulbull.performance.global.s3.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PerformanceServiceImpl implements PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final PerformanceImageRepository performanceImageRepository;
    private final PerformanceSongsRepository performanceSongsRepository;
    private final SongRepository songRepository;
    private final PersonRepository personRepository;
    private final BandSessionRepository bandSessionRepository;
    private final S3Uploader s3Uploader;

    @Override
    @Transactional
    public PerformanceDetailResponseDto createPerformance(
            PerformanceCreateBasicDto basicInfo,
            PerformanceSetlistRequestDto setlistRequest,
            MultipartFile posterFront,
            MultipartFile posterBack,
            List<MultipartFile> images) {

        // 1. Performance 엔티티 생성 및 저장 (제거된 필드는 null)
        Performance performance = Performance.builder()
                .websiteName(null)
                .websiteDescription(null)
                .title(basicInfo.title())
                .subtitle(null)
                .description(null)
                .location(basicInfo.location())
                .venue(basicInfo.venue())
                .dateTime(basicInfo.dateTime())
                .preSaleFee(basicInfo.preSaleFee())
                .onSiteFee(basicInfo.onSiteFee())
                .preSaleEndTime(basicInfo.preSaleEndTime())
                .posterFrontUrl(null)
                .posterBackUrl(null)
                .openchatUrl(basicInfo.phoneNumber())
                .phoneNumber(basicInfo.phoneNumber())
                .preSaleFormUrl(basicInfo.preSaleFormUrl())
                .bankName(basicInfo.bankName())
                .bankAccount(basicInfo.bankAccount())
                .accountHolder(basicInfo.accountHolder())
                .kakaopayUrl(basicInfo.kakaopayUrl())
                .naverpayUrl(basicInfo.naverpayUrl())
                .setlistUrl(basicInfo.setlistUrl())
                .currentSong(null)
                .build();

        performanceRepository.save(performance);

        Long performanceId = performance.getId();

        // 2. S3에 포스터 업로드
        String posterFrontUrl = null;
        String posterBackUrl = null;

        if (posterFront != null && !posterFront.isEmpty()) {
            posterFrontUrl = s3Uploader.upload(
                    posterFront,
                    "performance/" + performanceId + "/poster-front"
            );
        }
        if (posterBack != null && !posterBack.isEmpty()) {
            posterBackUrl = s3Uploader.upload(
                    posterBack,
                    "performance/" + performanceId + "/poster-back"
            );
        }

        // 3. 공연 이미지 S3 업로드 + PerformanceImage 저장
        if (images != null && !images.isEmpty()) {
            int index = 1;
            List<PerformanceImage> performanceImages = new ArrayList<>();
            for (MultipartFile image : images) {
                if (image == null || image.isEmpty()) continue;

                String url = s3Uploader.upload(
                        image,
                        "performance/" + performanceId + "/image" + index++
                );
                performanceImages.add(PerformanceImage.builder()
                        .imageUrl(url)
                        .performance(performance)
                        .build());
            }
            if (!performanceImages.isEmpty()) {
                performanceImageRepository.saveAll(performanceImages);
            }
        }

        // 4. Song 및 PerformanceSong 생성 (moreLinks 제거됨)
        if (setlistRequest.setlist() != null && !setlistRequest.setlist().isEmpty()) {
            for (var psDto : setlistRequest.setlist()) {
                Song song = songRepository.findByTitleAndArtist(
                                psDto.song().title(),
                                psDto.song().artist()
                        )
                        .orElseGet(() -> {
                            Song newSong = Song.builder()
                                    .title(psDto.song().title())
                                    .artist(psDto.song().artist())
                                    .album(psDto.song().album())
                                    .releaseDate(psDto.song().releaseDate())
                                    .genre(psDto.song().genre())
                                    .youtubeUrl(psDto.song().youtubeUrl())
                                    .albumImgUrl(psDto.song().albumImgUrl())
                                    .lyrics(psDto.song().lyrics())
                                    .build();
                            return songRepository.save(newSong);
                        });

                PerformanceSong performanceSong = PerformanceSong.builder()
                        .orderInPerformance(psDto.orderInPerformance())
                        .likes(0)
                        .performance(performance)
                        .song(song)
                        .build();
                performanceSongsRepository.save(performanceSong);

                if (psDto.members() != null) {
                    createBandSessions(performanceSong, psDto.members());
                }
            }
        }

        performance.setPosterFrontUrl(posterFrontUrl);
        performance.setPosterBackUrl(posterBackUrl);

        return getPerformanceDetail(performance.getId());
    }

    @Override
    public PerformanceDetailResponseDto getPerformanceDetail(Long performanceId) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(PerformanceNotFoundException::new); // 404: 존재하지 않는 공연

        // 공연 이미지 리스트 생성
        List<String> imageUrls = performanceImageRepository.findAllByPerformanceId(performance.getId())
                .stream()
                .map(PerformanceImage::getImageUrl)
                .toList();

        // 포스터 이미지 리스트 생성
        List<String> posterUrls = new ArrayList<>();
        posterUrls.add(performance.getPosterFrontUrl());
        posterUrls.add(performance.getPosterBackUrl());

        // dateTime: yyyy.MM.dd 형식
        String dateTimeFormatted = performance.getDateTime() != null
                ? performance.getDateTime().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                : "";

        return new PerformanceDetailResponseDto(
                performance.getId(),
                imageUrls,
                performance.getTitle(),
                dateTimeFormatted,
                performance.getVenue(),
                performance.getPreSaleFormUrl(),
                performance.getPhoneNumber(),
                posterUrls,
                performance.getLocation()
        );
    }

    // 공연 셋리스트 조회
    @Override
    public PerformanceSetlistResponse getPerformanceSetlist(Long performanceId) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(PerformanceNotFoundException::new); // 404: 존재하지 않는 공연

        List<PerformanceSong> performanceSongs = performanceSongsRepository.findByPerformanceId(performance.getId());

        // performanceSongs를 PerformanceSetListDetail 리스트로 변환 및 정렬
        List<PerformanceSetListDetail> setList = performanceSongs.stream()
                .map(p -> new PerformanceSetListDetail(
                        p.getOrderInPerformance(),
                        p.getId(),
                        p.getSong().getTitle(),
                        p.getSong().getArtist()
                ))
                .sorted(Comparator.comparingInt(PerformanceSetListDetail::order)) // order 기준 정렬
                .toList();

        // currentSong이 null일 수 있는 경우 처리
        int currentSongId = performance.getCurrentSong() != null
                ? performance.getCurrentSong().getOrderInPerformance()
                : -1;

        return new PerformanceSetlistResponse(currentSongId, performance.getSetlistUrl(), setList);
    }

    // BandSession 생성 헬퍼 메서드
    private void createBandSessions(PerformanceSong performanceSong, PerformanceSetlistRequestDto.MembersDto membersDto) {
        List<BandSession> bandSessions = new ArrayList<>();

        // Vocal
        if (membersDto.vocal() != null) {
            for (String personName : membersDto.vocal()) {
                Person person = getOrCreatePerson(personName);
                bandSessions.add(BandSession.builder()
                        .sessionType(SessionType.V)
                        .performanceSong(performanceSong)
                        .band(null) // Band 정보는 현재 없음
                        .person(person)
                        .build());
            }
        }

        // Guitar1
        if (membersDto.guitar1() != null) {
            for (String personName : membersDto.guitar1()) {
                Person person = getOrCreatePerson(personName);
                bandSessions.add(BandSession.builder()
                        .sessionType(SessionType.G1)
                        .performanceSong(performanceSong)
                        .band(null)
                        .person(person)
                        .build());
            }
        }

        // Guitar2
        if (membersDto.guitar2() != null) {
            for (String personName : membersDto.guitar2()) {
                Person person = getOrCreatePerson(personName);
                bandSessions.add(BandSession.builder()
                        .sessionType(SessionType.G2)
                        .performanceSong(performanceSong)
                        .band(null)
                        .person(person)
                        .build());
            }
        }

        // Bass
        if (membersDto.bass() != null) {
            for (String personName : membersDto.bass()) {
                Person person = getOrCreatePerson(personName);
                bandSessions.add(BandSession.builder()
                        .sessionType(SessionType.B)
                        .performanceSong(performanceSong)
                        .band(null)
                        .person(person)
                        .build());
            }
        }

        // Drum
        if (membersDto.drum() != null) {
            for (String personName : membersDto.drum()) {
                Person person = getOrCreatePerson(personName);
                bandSessions.add(BandSession.builder()
                        .sessionType(SessionType.D)
                        .performanceSong(performanceSong)
                        .band(null)
                        .person(person)
                        .build());
            }
        }

        // Keyboard
        if (membersDto.keyboard() != null) {
            for (String personName : membersDto.keyboard()) {
                Person person = getOrCreatePerson(personName);
                bandSessions.add(BandSession.builder()
                        .sessionType(SessionType.K)
                        .performanceSong(performanceSong)
                        .band(null)
                        .person(person)
                        .build());
            }
        }

        // 모든 BandSession 저장
        if (!bandSessions.isEmpty()) {
            bandSessionRepository.saveAll(bandSessions);
        }
    }

    // Person 조회 또는 생성
    private Person getOrCreatePerson(String name) {
        return personRepository.findByName(name)
                .orElseGet(() -> {
                    Person newPerson = Person.builder()
                            .name(name)
                            .build();
                    return personRepository.save(newPerson);
                });
    }

    // 공연 이미지 교체
    @Override
    @Transactional
    public void replacePerformanceImages(Long performanceId, List<MultipartFile> newImages) {

        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(PerformanceNotFoundException::new);

//        // 1) S3에서 해당 performance 폴더 전체 삭제
        String folderPrefix = "performance/" + performanceId + "/";
//        s3Uploader.deleteFolder(folderPrefix);

        // 2) DB 기존 이미지 삭제
        performanceImageRepository.deleteAllByPerformanceId(performanceId);

        // 3) 새 이미지 업로드
        List<String> newUrls = new ArrayList<>();

        if (newImages != null && !newImages.isEmpty()) {
            for (MultipartFile image : newImages) {
                if (image == null || image.isEmpty()) continue;

                String url = s3Uploader.upload(
                        image,
                        folderPrefix + UUID.randomUUID()  // index 대신 uuid 사용
                );

                newUrls.add(url);
            }
        }

        // 4) DB 저장
        if (!newUrls.isEmpty()) {
            List<PerformanceImage> imgs = newUrls.stream()
                    .map(url -> PerformanceImage.builder()
                            .performance(performance)
                            .imageUrl(url)
                            .build())
                    .toList();

            performanceImageRepository.saveAll(imgs);
        }

        System.out.println("[공연 이미지 교체 완료] performanceId=" + performanceId);
    }
}
