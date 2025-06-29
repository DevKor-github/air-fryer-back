package com.airfryer.repicka.common.aws.s3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PresignedUrlReq {
    
    @NotBlank(message = "파일명을 입력해주세요")
    private String fileName;
    
    @NotBlank(message = "파일 타입을 입력해주세요")
    private String contentType;
    
    @NotNull(message = "파일 크기를 입력해주세요")
    @Positive(message = "파일 크기는 0보다 커야 합니다")
    private Long fileSize;
} 