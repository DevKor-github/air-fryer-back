package com.airfryer.repicka.domain.user;

import com.airfryer.repicka.domain.item.dto.res.OwnedItemListRes;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item.repository.ItemRepository;
import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import com.airfryer.repicka.domain.item_image.repository.ItemImageRepository;
import com.airfryer.repicka.domain.user.dto.ReportUserReq;
import com.airfryer.repicka.domain.user.entity.user_report.UserReport;
import com.airfryer.repicka.domain.user.repository.UserReportRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.aws.s3.S3Service;
import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlReq;
import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlRes;
import com.airfryer.repicka.domain.user.entity.user.User;
import com.airfryer.repicka.domain.user.dto.BaseUserDto;
import com.airfryer.repicka.domain.user.dto.UpdateUserReq;
import com.airfryer.repicka.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserReportRepository userReportRepository;
    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;
    private final S3Service s3Service;

    // fcm 토큰 업데이트
    @Transactional
    public void updateFcmToken(Long userId, String fcmToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, null));
        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }

    // 푸시 알림 활성화 여부 업데이트
    @Transactional
    public void updatePush(Long userId, Boolean isPushEnabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, null));
        user.setIsPushEnabled(isPushEnabled);
        userRepository.save(user);
    }

    // S3 Presigned URL 조회
    public PresignedUrlRes getPresignedUrl(PresignedUrlReq req) {
        return s3Service.generatePresignedUrl(req, "profile");
    }

    // 프로필 조회
    @Transactional(readOnly = true)
    public BaseUserDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, null));
        return BaseUserDto.from(user);
    }

    // 프로필 업데이트
    @Transactional
    public BaseUserDto updateProfile(Long userId, UpdateUserReq req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, null));
        user.updateProfile(req);
        userRepository.save(user);

        return BaseUserDto.from(user);
    }

    // 유저 신고
    @Transactional
    public void reportUser(User reporter, ReportUserReq dto)
    {
        /// 피신고자 조회

        // 피신고자 조회
        User reported = userRepository.findById(dto.getReportedUserId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, dto.getReportedUserId()));

        // 본인이 본인을 신고하는 경우, 예외 처리
        if(Objects.equals(reporter.getId(), reported.getId())) {
            throw new CustomException(CustomExceptionCode.SAME_REPORTER_AND_REPORTED, null);
        }

        /// 제품 조회

        // 제품 조회
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.ITEM_NOT_FOUND, dto.getItemId()));

        // 연관 없는 제품일 경우, 예외 처리
        if(
                !Objects.equals(item.getOwner().getId(), reporter.getId()) &&
                !Objects.equals(item.getOwner().getId(), reported.getId())
        )
        {
            throw new CustomException(CustomExceptionCode.UNRELATED_ITEM, null);
        }

        /// 유저 신고 데이터 존재 여부에 따른 분기 처리

        // 기존의 유저 신고 데이터 조회
        Optional<UserReport> userReportOptional = userReportRepository.findByReporterIdAndReportedIdAndItemId(
                reporter.getId(),
                reported.getId(),
                item.getId()
        );

        // 기존의 유저 신고 데이터가 존재한다면, 기존 데이터를 수정
        // 기존의 유저 신고 데이터가 존재하지 않으면, 새로운 데이터를 생성
        if(userReportOptional.isPresent())
        {
            UserReport userReport = userReportOptional.get();

            userReport.update(dto);
        }
        else
        {
            UserReport userReport = UserReport.builder()
                    .reporter(reporter)
                    .reported(reported)
                    .item(item)
                    .location(dto.getLocation())
                    .categories(dto.getCategories())
                    .description(dto.getDescription())
                    .build();

            userReportRepository.save(userReport);
        }
    }

    // 소유한 제품 리스트 조회
    @Transactional(readOnly = true)
    public List<OwnedItemListRes> getOwnedItemList(Long userId)
    {
        // 제품 리스트 조회
        List<Item> itemList = itemRepository.findAllByOwnerIdAndIsDeletedFalse(userId);

        return itemList.stream().map(item -> {

            // 대표 이미지 조회
            ItemImage thumbnail = itemImageRepository.findFirstByItemId(item.getId())
                    .orElseThrow(() -> new CustomException(CustomExceptionCode.ITEM_IMAGE_NOT_FOUND, null));

            // 판매 여부
            boolean isSold = item.getSaleDate() != null;

            return OwnedItemListRes.from(item, thumbnail.getFileKey(), isSold);

        }).toList();
    }
}
