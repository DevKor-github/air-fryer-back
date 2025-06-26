package com.airfryer.repicka.common.aws.s3;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.airfryer.repicka.common.response.SuccessResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    // 테스트용 이미지 업로드 api
    @PostMapping("/image")
    public ResponseEntity<SuccessResponseDto> uploadFile(
            @RequestParam(value = "image", required = false) MultipartFile image)
    {
        String imageUrl = s3Service.uploadImage(image, "image");
        return ResponseEntity.ok(SuccessResponseDto.builder()
                .message("이미지 업로드 성공")
                .data(imageUrl)
                .build());
    }

    @PostMapping("/images")
    public ResponseEntity<SuccessResponseDto> uploadImages(
            @RequestParam(value = "images", required = false) MultipartFile[] images)  
    {
        String[] imageUrls = s3Service.uploadImages(images, "images");
        return ResponseEntity.ok(SuccessResponseDto.builder()
                .message("이미지 업로드 성공")
                .data(imageUrls)
                .build());
    }

}
