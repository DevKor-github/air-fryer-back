package com.airfryer.repicka.common.aws.s3;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    
    private final S3Template s3Template;
    
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
    
    // 단일 이미지 업로드
    public String uploadImage(MultipartFile file, String directory) {
        try {
            validateFile(file);
            
            String fileName = directory + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            
            // S3Template을 사용한 파일 업로드
            s3Template.upload(bucket, fileName, file.getInputStream());
            return cloudfrontDomain + "/" + fileName;
            
        } catch (IOException e) {
            throw new CustomException(CustomExceptionCode.FILE_UPLOAD_FAILED, file.getOriginalFilename());
        }
    }
    
    // 다중 이미지 업로드
    public String[] uploadImages(MultipartFile[] files, String directory) {
        if (files == null || files.length == 0) {
            throw new CustomException(CustomExceptionCode.FILE_NOT_FOUND, "업로드할 파일이 없습니다");
        }
        
        return Arrays.stream(files)
            .map(file -> uploadImage(file, directory))
            .toArray(String[]::new);
    }
    
    // 파일 유효성 검사
    private void validateFile(MultipartFile file) {
        // 파일이 비어있는지 확인
        if (file == null || file.isEmpty()) {
            throw new CustomException(CustomExceptionCode.FILE_NOT_FOUND, "업로드할 파일이 없습니다");
        }
        
        // 파일 크기 확인
        if (file.getSize() > maxFileSize) {
            throw new CustomException(CustomExceptionCode.FILE_SIZE_EXCEEDED, 
                String.format("파일 크기: %d bytes, 최대 허용: %d bytes", file.getSize(), maxFileSize));
        }
        
        // 파일 확장자 확인
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isValidImageExtension(originalFilename)) {
            throw new CustomException(CustomExceptionCode.INVALID_FILE_FORMAT, 
                "허용되는 이미지 형식: " + String.join(", ", getAllowedExtensions()));
        }
    }
    
    // 이미지 파일 확장자 검사
    private boolean isValidImageExtension(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return getAllowedExtensions().contains(extension);
    }
} 