package com.airfryer.repicka.domain.item;

import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlReq;
import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlRes;
import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.appointment.dto.GetItemAvailabilityRes;
import com.airfryer.repicka.domain.item.dto.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/item")
@RequiredArgsConstructor
public class ItemController
{
    private final ItemService itemService;

    // S3 Presigned URL 조회
    @GetMapping("/presigned-url")    
    public ResponseEntity<SuccessResponseDto> getPresignedUrl(@Valid PresignedUrlReq req)
    {
        PresignedUrlRes presignedUrlRes = itemService.getPresignedUrl(req);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("Presigned URL을 성공적으로 생성하였습니다.")
                        .data(presignedUrlRes)
                        .build());
    }

    // 제품 생성
    @PostMapping
    public ResponseEntity<SuccessResponseDto> createItem(@AuthenticationPrincipal CustomOAuth2User user,
                                                         @Valid @RequestBody CreateItemReq req)
    {
        ItemDetailRes itemDetailRes = itemService.createItemAndImages(req, user.getUser());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .message("제품을 성공적으로 생성하였습니다.")
                        .data(itemDetailRes)
                        .build());
    }

    // 제품 수정
    @PutMapping("/{itemId}")
    public ResponseEntity<SuccessResponseDto> updateItem(@AuthenticationPrincipal CustomOAuth2User user,
                                                         @PathVariable(value="itemId") Long itemId,
                                                         @Valid @RequestBody CreateItemReq req)
    {
        ItemDetailRes itemDetailRes = itemService.updateItem(itemId, req, user.getUser());

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("제품을 성공적으로 수정하였습니다.")
                        .data(itemDetailRes)
                        .build());
    }

    // 제품 삭제
    @DeleteMapping("/{itemId}")
    public ResponseEntity<SuccessResponseDto> deleteItem(@AuthenticationPrincipal CustomOAuth2User user,
                                                         @PathVariable(value="itemId") Long itemId)
    {
        itemService.deleteItem(itemId, user.getUser());

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("제품을 성공적으로 삭제하였습니다.")
                        .build());
    }

    // 제품 상세 조회
    @GetMapping("/{itemId}")
    public ResponseEntity<SuccessResponseDto> getItemDetail(@PathVariable(value="itemId") Long itemId, 
                                                            @AuthenticationPrincipal CustomOAuth2User user)
    {
        // JWT 토큰이 있는 경우 User 정보 전달
        ItemDetailRes itemDetailRes = itemService.getItemDetail(itemId, user != null ? user.getUser() : null);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("제품 상세 내용을 성공적으로 조회하였습니다.")
                        .data(itemDetailRes)
                        .build());
    }

    // 제품 목록 검색
    @GetMapping("/search")
    public ResponseEntity<SuccessResponseDto> searchItemList(@Valid SearchItemReq req)
    {
        // 디버깅: 어떤 itemOrder가 적용되었는지 로그 출력
        log.info("🔍 검색 요청 - itemOrder: {}, pageSize: {}, color: {}", req.getItemOrder(), req.getPageSize(), req.getColors() != null ? req.getColors().length : 0);
        
        SearchItemRes data = itemService.searchItemList(req);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("조건에 따른 제품 목록을 성공적으로 조회하였습니다.")
                        .data(data)
                        .build());
    }

    // 제품 끌올
    @PatchMapping("/{itemId}/repost")
    public ResponseEntity<SuccessResponseDto> repostItem(@AuthenticationPrincipal CustomOAuth2User user,
                                                         @PathVariable(value="itemId") Long itemId) {
        LocalDateTime repostDate = itemService.repostItem(itemId, user.getUser());

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()              
                        .message("제품을 성공적으로 끌올하였습니다.")
                        .data(Map.of("repostDate", repostDate))
                        .build());
    }

    // 월 단위로 날짜별 제품 대여 가능 여부 조회
    @GetMapping("/{postId}/rental-availability")
    public ResponseEntity<SuccessResponseDto> getItemRentalAvailability(@PathVariable Long postId,
                                                                        @RequestParam int year,
                                                                        @RequestParam int month)
    {
        GetItemAvailabilityRes data = itemService.getItemRentalAvailability(postId, year, month);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("날짜별 제품 대여 가능 여부를 성공적으로 조회하였습니다.")
                        .data(data)
                        .build());
    }

    // 제품 구매가 가능한 첫 날짜 조회
    @GetMapping("/{itemId}/sale-availability")
    public ResponseEntity<SuccessResponseDto> getItemSaleAvailability(@PathVariable Long itemId)
    {
        LocalDate data = itemService.getItemSaleAvailability(itemId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("제품 구매가 가능한 첫 날짜를 성공적으로 조회하였습니다.")
                        .data(data)
                        .build());
    }

}
