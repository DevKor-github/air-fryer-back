package com.airfryer.repicka.common.aws.s3.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PresignedUrlRes {
    private String presignedUrl;
    private String fileKey;
    private LocalDateTime expiresAt;
} 