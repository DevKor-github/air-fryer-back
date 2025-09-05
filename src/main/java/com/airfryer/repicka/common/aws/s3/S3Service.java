package com.airfryer.repicka.common.aws.s3;

import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlReq;
import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlRes;
import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.cloudfront.domain}")
    private String cloudfrontDomain;
    
    @Value("${file.upload.max-size}")
    private long maxFileSize;
    
    @Value("${file.upload.allowed-extensions}")
    private String allowedExtensionsString;
    
    private List<String> getAllowedExtensions() {
        return Arrays.asList(allowedExtensionsString.split(","));
    }
    
    // 이미지 직접 업로드 (fileKey 반환)
    public String uploadImage(MultipartFile file, String directory) {
        try {
            if (file == null || file.isEmpty()) {
                throw new CustomException(CustomExceptionCode.FILE_NOT_FOUND, "업로드할 파일이 없습니다");
            }
            validateFileProperties(file.getOriginalFilename(), file.getSize(), file.getContentType());    
            
            String fileKey = directory + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            
            // S3Client를 사용한 파일 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return fileKey;
            
        } catch (IOException e) {
            throw new CustomException(CustomExceptionCode.FILE_UPLOAD_FAILED, file.getOriginalFilename());
        }
    }
    
    // Presigned URL 생성
    public PresignedUrlRes generatePresignedUrl(PresignedUrlReq presignedUrlReq, String directory) {
        validateFileProperties(presignedUrlReq.getFileName(), presignedUrlReq.getFileSize().longValue(), presignedUrlReq.getContentType());
        
        String fileKey = directory + "/" + UUID.randomUUID() + "_" + presignedUrlReq.getFileName();
        
        // file metadata를 담은 PutObjectRequest 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .contentType(presignedUrlReq.getContentType())
                .contentLength(presignedUrlReq.getFileSize())
                .build();
        
        // Presigned URL 생성 (10분 유효)
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();
        
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        
        return PresignedUrlRes.builder()
                .presignedUrl(presignedRequest.url().toString())
                .fileKey(fileKey)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
    }
    
    // 공통 파일 속성 유효성 검사
    private void validateFileProperties(String fileName, long fileSize, String contentType) {
        // 파일 크기 확인
        if (fileSize > maxFileSize) {
            throw new CustomException(CustomExceptionCode.FILE_SIZE_EXCEEDED,
                String.format("파일 크기: %d bytes, 최대 허용: %d bytes", fileSize, maxFileSize));
        }
        
        // 파일 확장자 추출
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            throw new CustomException(CustomExceptionCode.INVALID_FILE_FORMAT, "파일 확장자가 없습니다");
        }
        
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        
        // 확장자와 Content-Type 일치 여부 확인
        if (!isValidExtensionAndContentType(extension, contentType)) {
            throw new CustomException(CustomExceptionCode.INVALID_FILE_FORMAT,
                String.format("파일 확장자(%s)와 Content-Type(%s)이 일치하지 않습니다", extension, contentType));
        }
    }
    
    // 확장자와 Content-Type 일치 여부 확인
    private boolean isValidExtensionAndContentType(String extension, String contentType) {
        if (contentType == null) {
            return false;
        }
        
        // 확장자와 Content-Type 매핑 관계
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image/jpeg".equals(contentType.toLowerCase());
            case "png":
                return "image/png".equals(contentType.toLowerCase());
            case "webp":
                return "image/webp".equals(contentType.toLowerCase());
            case "heic":
                return "image/heic".equals(contentType.toLowerCase()) || "image/heif".equals(contentType.toLowerCase());
            default:
                return false; // 허용되지 않는 확장자
        }
    }
    
    // fileKey를 전체 CloudFront URL로 변환
    public String getFullImageUrl(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            return null;
        }
        return cloudfrontDomain + "/" + fileKey;
    }
    
} 