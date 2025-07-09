package com.airfryer.repicka.domain.appointment.controller;

import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.appointment.FindMyAppointmentSubject;
import com.airfryer.repicka.domain.appointment.dto.*;
import com.airfryer.repicka.domain.appointment.service.AppointmentService;
import com.airfryer.repicka.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointment")
@RequiredArgsConstructor
public class AppointmentController
{
    private final AppointmentService appointmentService;

    // 게시글에서 대여 약속 제시
    @PostMapping("/rental")
    public ResponseEntity<SuccessResponseDto> offerRentalAppointment(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                     @RequestBody @Valid OfferRentalAppointmentReq dto)
    {
        User borrower = oAuth2User.getUser();
        appointmentService.offerRentalAppointment(borrower, dto);

        // TODO: 채팅방 데이터와 약속 데이터를 응답해야 함.
        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("대여 약속을 성공적으로 제시하였습니다.")
                        .data(null)
                        .build());
    }

    // 게시글에서 판매 약속 제시
    @PostMapping("/sale")
    public ResponseEntity<SuccessResponseDto> offerSaleAppointment(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                   @RequestBody @Valid OfferSaleAppointmentReq dto)
    {
        User buyer = oAuth2User.getUser();
        appointmentService.offerSaleAppointment(buyer, dto);

        // TODO: 채팅방 데이터를 data로 응답해야 함.
        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("판매 약속을 성공적으로 제시하였습니다.")
                        .data(null)
                        .build());
    }

    // TODO: 채팅방에서 대여 약속 제시 API 구현
    // TODO: 채팅방에서 판매 약속 제시 API 구현

    // 약속 확정
    @PatchMapping("/{appointmentId}/confirm")
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
    @PatchMapping("/{appointmentId}/cancel")
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
    @GetMapping("/requester")
    public ResponseEntity<SuccessResponseDto> findMyAppointmentPageAsRequester(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                               @Valid FindMyAppointmentPageReq dto)
    {
        User requester = oAuth2User.getUser();
        AppointmentPageRes data = appointmentService.findMyAppointmentPage(
                requester,
                FindMyAppointmentSubject.REQUESTER,
                dto
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("내가 requester인 약속 페이지를 성공적으로 조회하였습니다.")
                        .data(data)
                        .build());
    }

    // 내가 owner인 약속 페이지 조회
    // 요청자가 owner인 (확정/대여중/완료) 상태의 약속 페이지 조회
    @GetMapping("/owner")
    public ResponseEntity<SuccessResponseDto> findMyAppointmentPageAsOwner(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                           @Valid FindMyAppointmentPageReq dto)
    {
        User requester = oAuth2User.getUser();
        AppointmentPageRes data = appointmentService.findMyAppointmentPage(
                requester,
                FindMyAppointmentSubject.OWNER,
                dto
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("내가 owner인 약속 페이지를 성공적으로 조회하였습니다.")
                        .data(data)
                        .build());
    }

    // 확정된 약속 변경 제시
    @PatchMapping("/confirmed")
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
}
