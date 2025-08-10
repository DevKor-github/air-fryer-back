package com.airfryer.repicka.domain.user;

import org.springframework.stereotype.Service;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.aws.s3.S3Service;
import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlReq;
import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlRes;
import com.airfryer.repicka.domain.user.entity.User;
import com.airfryer.repicka.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    // fcm 토큰 업데이트
    public void updateFcmToken(Long userId, String fcmToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, null));
        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }

    // 푸시 알림 활성화 여부 업데이트
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
}
