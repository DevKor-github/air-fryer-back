package com.airfryer.repicka.domain.review;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.review.dto.ReviewReq;
import com.airfryer.repicka.domain.review.entity.Review;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/review")
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 생성
    @PostMapping
    public ResponseEntity<SuccessResponseDto> createReview(@AuthenticationPrincipal CustomOAuth2User user, 
                                                            @RequestBody @Valid ReviewReq reviewReq) {
        reviewService.createReview(reviewReq, user.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(SuccessResponseDto.builder()
            .message("리뷰가 성공적으로 생성되었습니다.")
            .build());
    }
    
    // 리뷰 조회
    @GetMapping("/{userId}")
    public ResponseEntity<SuccessResponseDto> getReview(@PathVariable Long userId) {
        List<Review> data = reviewService.getReview(userId);
        return ResponseEntity.ok(SuccessResponseDto.builder()
            .data(data)
            .message("리뷰가 성공적으로 조회되었습니다.")
            .build());
    }
}
