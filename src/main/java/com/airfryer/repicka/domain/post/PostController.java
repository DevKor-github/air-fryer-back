package com.airfryer.repicka.domain.post;

import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlReq;
import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlRes;
import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.appointment.dto.GetItemAvailabilityRes;
import com.airfryer.repicka.domain.post.dto.CreatePostReq;
import com.airfryer.repicka.domain.post.dto.PostDetailRes;
import com.airfryer.repicka.domain.post.dto.PostPreviewRes;
import com.airfryer.repicka.domain.post.dto.SearchPostReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/post")
public class PostController {
    private final PostService postService;

    @GetMapping("/presigned-url")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> getPresignedUrl(@AuthenticationPrincipal CustomOAuth2User user,
                                                              @Valid PresignedUrlReq req) {
        PresignedUrlRes presignedUrlRes = postService.getPresignedUrl(req, user.getUser());

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("Presigned URL을 성공적으로 생성하였습니다.")
                        .data(presignedUrlRes)
                        .build());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> createPost(@AuthenticationPrincipal CustomOAuth2User user,
                                                         @Valid @RequestBody CreatePostReq req) {
        List<PostDetailRes> postDetailResList = postService.createPostWithItemAndImages(req, user.getUser());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .message("게시글을 성공적으로 생성하였습니다.")
                        .data(postDetailResList)
                        .build());
    }

    @PutMapping("/{postId}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> updatePost(@AuthenticationPrincipal CustomOAuth2User user,
                                                          @PathVariable(value="postId") Long postId,
                                                          @Valid @RequestBody CreatePostReq req) {
        List<PostDetailRes> postDetailResList = postService.updatePost(postId, req, user.getUser());

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("게시글을 성공적으로 수정하였습니다.")
                        .data(postDetailResList)
                        .build());
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> deletePost(@AuthenticationPrincipal CustomOAuth2User user,
                                                          @PathVariable(value="postId") Long postId) {
        postService.deletePost(postId, user.getUser());

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("게시글을 성공적으로 삭제하였습니다.")
                        .build());
    }

    @GetMapping("/{postId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> getPostDetail(@PathVariable(value="postId") Long postId,
                                                           @AuthenticationPrincipal CustomOAuth2User user) {
        PostDetailRes postDetailRes = postService.getPostDetail(postId, user);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("게시글 상세 내용을 성공적으로 조회하였습니다.")
                        .data(postDetailRes)
                        .build());
    }

    @GetMapping("/search")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> searchPostList(@Valid SearchPostReq req) {
        List<PostPreviewRes> postPreviewResList = postService.searchPostList(req);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("조건에 따른 게시글 목록을 성공적으로 조회하였습니다.")
                        .data(postPreviewResList)
                        .build());
    }

    // 월 단위로 날짜별 제품 대여 가능 여부 조회
    @GetMapping("/{postId}/rental-availability")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> getItemRentalAvailability(@PathVariable Long postId,
                                                                        @RequestParam int year,
                                                                        @RequestParam int month)
    {
        GetItemAvailabilityRes data = postService.getItemRentalAvailability(postId, year, month);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("날짜별 제품 대여 가능 여부를 성공적으로 조회하였습니다.")
                        .data(data)
                        .build());
    }

    // 제품 구매가 가능한 첫 날짜 조회
    @GetMapping("/{postId}/sale-availability")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> getItemSaleAvailability(@PathVariable Long postId)
    {
        LocalDate data = postService.getItemSaleAvailability(postId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("제품 구매가 가능한 첫 날짜를 성공적으로 조회하였습니다.")
                        .data(data)
                        .build());
    }

}
