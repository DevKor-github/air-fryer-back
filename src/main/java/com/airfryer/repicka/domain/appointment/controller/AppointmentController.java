package com.airfryer.repicka.domain.appointment.controller;

import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.appointment.FindMyAppointmentPeriod;
import com.airfryer.repicka.domain.appointment.FindMyAppointmentSubject;
import com.airfryer.repicka.domain.appointment.dto.*;
import com.airfryer.repicka.domain.appointment.service.AppointmentService;
import com.airfryer.repicka.domain.post.entity.PostType;
import com.airfryer.repicka.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AppointmentController
{
    private final AppointmentService appointmentService;

    // 대여 게시글에서 약속 제시
    @PostMapping("/appointment/in-rental-post")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> offerAppointmentInRentalPost(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                           @RequestBody @Valid OfferAppointmentInRentalPostReq dto)
    {
        User borrower = oAuth2User.getUser();
        appointmentService.offerAppointmentInRentalPost(borrower, dto);

        // TODO: 채팅방 데이터와 약속 데이터를 응답해야 함.
        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("대여 게시글에서 약속을 성공적으로 제시하였습니다.")
                        .data(null)
                        .build());
    }

    // 판매 게시글에서 약속 제시
    @PostMapping("/appointment/in-sale-post")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> offerAppointmentInSalePost(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                         @RequestBody @Valid OfferAppointmentInSalePostReq dto)
    {
        User buyer = oAuth2User.getUser();
        appointmentService.offerAppointmentInSalePost(buyer, dto);

        // TODO: 채팅방 데이터를 data로 응답해야 함.
        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("판매 게시글에서 약속을 성공적으로 제시하였습니다.")
                        .data(null)
                        .build());
    }

    // TODO: 채팅방이 구현되면, 채팅방에서 대여 약속 및 판매 약속 제시 API 구현

    // 월 단위로 날짜별 제품 대여 가능 여부 조회
    @GetMapping("/post/{postId}/rental-availability")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> getItemRentalAvailability(@PathVariable Long postId,
                                                                        @RequestParam int year,
                                                                        @RequestParam int month)
    {
        GetItemAvailabilityRes data = appointmentService.getItemRentalAvailability(postId, year, month);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("날짜별 제품 대여 가능 여부를 성공적으로 조회하였습니다.")
                        .data(data)
                        .build());
    }

    // 제품 구매가 가능한 첫 날짜 조회
    @GetMapping("/post/{postId}/sale-availability")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> getItemSaleAvailability(@PathVariable Long postId)
    {
        LocalDate data = appointmentService.getItemSaleAvailability(postId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("제품 구매가 가능한 첫 날짜를 성공적으로 조회하였습니다.")
                        .data(data)
                        .build());
    }

    // 약속 확정
    @PatchMapping("/appointment/{appointmentId}/confirm")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> confirmAppointment(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                 @PathVariable Long appointmentId)
    {
        User user = oAuth2User.getUser();
        AppointmentRes data = appointmentService.confirmAppointment(user, appointmentId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("약속을 성공적으로 확정하였습니다.")
                        .data(data)
                        .build());
    }

    // 약속 취소
    @PatchMapping("/appointment/{appointmentId}/cancel")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> cancelAppointment(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                @PathVariable Long appointmentId)
    {
        User user = oAuth2User.getUser();
        AppointmentRes data = appointmentService.cancelAppointment(user, appointmentId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("약속을 성공적으로 취소하였습니다.")
                        .data(data)
                        .build());
    }

    // 내가 requester인 약속 페이지 조회 (나의 PICK 조회)
    // 요청자가 requester인 (확정/대여중/완료) 상태의 약속 페이지 조회
    @GetMapping("/appointment/requester")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> findMyAppointmentPageAsRequester(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                               Pageable pageable,
                                                                               @RequestParam PostType type,
                                                                               @RequestParam FindMyAppointmentPeriod period)
    {
        User requester = oAuth2User.getUser();
        AppointmentPageRes data = appointmentService.findMyAppointmentPage(
                requester,
                pageable,
                type,
                FindMyAppointmentSubject.REQUESTER,
                period
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("내가 requester인 약속 페이지를 성공적으로 조회하였습니다.")
                        .data(data)
                        .build());
    }

    // 내가 owner인 약속 페이지 조회
    // 요청자가 owner인 (확정/대여중/완료) 상태의 약속 페이지 조회
    @GetMapping("/appointment/owner")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> findMyAppointmentPageAsOwner(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                           Pageable pageable,
                                                                           @RequestParam PostType type,
                                                                           @RequestParam FindMyAppointmentPeriod period)
    {
        User requester = oAuth2User.getUser();
        AppointmentPageRes data = appointmentService.findMyAppointmentPage(
                requester,
                pageable,
                type,
                FindMyAppointmentSubject.OWNER,
                period
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("내가 owner인 약속 페이지를 성공적으로 조회하였습니다.")
                        .data(data)
                        .build());
    }

    // 확정된 약속 변경 제시
    @PatchMapping("/confirmed-appointment")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> offerToUpdateConfirmedAppointment(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                                @RequestBody @Valid OfferToUpdateConfirmedAppointmentReq dto)
    {
        User user = oAuth2User.getUser();
        AppointmentRes data = appointmentService.offerToUpdateConfirmedAppointment(user, dto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("확정된 약속의 변경을 성공적으로 제시하였습니다.")
                        .data(data)
                        .build());
    }

    // 대여 중인 약속 변경 제시
    @PostMapping("/update-in-progress-appointment")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> offerToUpdateInProgressAppointment(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                                 @RequestBody @Valid OfferToUpdateInProgressAppointmentReq dto)
    {
        User user = oAuth2User.getUser();
        AppointmentRes data = appointmentService.offerToUpdateInProgressAppointment(user, dto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("대여 중인 약속의 변경을 성공적으로 제시하였습니다.")
                        .data(data)
                        .build());
    }

    // 대여 중인 약속 변경 제시 데이터 조회
    @GetMapping("/update-in-progress-appointment")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> findOfferToUpdateInProgressAppointment(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                                     @RequestParam Long appointmentId,
                                                                                     @RequestParam Boolean isMine)
    {
        User user = oAuth2User.getUser();
        UpdateInProgressAppointmentRes data = appointmentService.findOfferToUpdateInProgressAppointment(user, appointmentId, isMine);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("대여 중인 약속 변경 제시 데이터를 성공적으로 조회하였습니다.")
                        .data(data)
                        .build());
    }

    // 대여 중인 약속 변경 제시 수락 및 거절
    @PatchMapping("/update-in-progress-appointment/{updateInProgressAppointmentId}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> acceptOfferToUpdateInProgressAppointment(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                                       @PathVariable Long updateInProgressAppointmentId,
                                                                                       @RequestParam Boolean isAccepted)
    {
        User user = oAuth2User.getUser();
        appointmentService.responseOfferToUpdateInProgressAppointment(user, updateInProgressAppointmentId, isAccepted);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("대여 중인 약속 변경 제시를 성공적으로 " + (isAccepted ? "수락" : "거절") + "하였습니다.")
                        .data(null)
                        .build());
    }

    // 대여 중인 약속 변경 제시 취소
    @DeleteMapping("/update-in-progress-appointment/{updateInProgressAppointmentId}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> deleteOfferToUpdateInProgressAppointment(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                                       @PathVariable Long updateInProgressAppointmentId)
    {
        User user = oAuth2User.getUser();
        appointmentService.deleteOfferToUpdateInProgressAppointment(user, updateInProgressAppointmentId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("대여 중인 약속 변경 제시를 성공적으로 취소하였습니다.")
                        .data(null)
                        .build());
    }
}
