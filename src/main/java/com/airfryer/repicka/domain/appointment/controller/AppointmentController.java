package com.airfryer.repicka.domain.appointment.controller;

import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.appointment.dto.*;
import com.airfryer.repicka.domain.appointment.service.AppointmentService;
import com.airfryer.repicka.domain.chat.dto.EnterChatRoomRes;
import com.airfryer.repicka.domain.user.entity.user.User;
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

    // 대여 약속 제시
    @PostMapping("/rental")
    public ResponseEntity<SuccessResponseDto> offerRentalAppointment(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                     @RequestBody @Valid OfferAppointmentReq dto)
    {
        User borrower = oAuth2User.getUser();
        EnterChatRoomRes data = appointmentService.proposeAppointment(borrower, dto, true);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .message("대여 약속을 성공적으로 제시하였습니다.")
                        .data(data)
                        .build());
    }

    // 구매 약속 제시
    @PostMapping("/sale")
    public ResponseEntity<SuccessResponseDto> offerSaleAppointment(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                   @RequestBody @Valid OfferAppointmentReq dto)
    {
        User buyer = oAuth2User.getUser();
        EnterChatRoomRes data = appointmentService.proposeAppointment(buyer, dto, false);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .message("구매 약속을 성공적으로 제시하였습니다.")
                        .data(data)
                        .build());
    }

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

    // 약속 상세 조회
    @GetMapping("/{appointmentId}")
    public ResponseEntity<SuccessResponseDto> getAppointmentDetail(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                   @PathVariable Long appointmentId)
    {
        User user = oAuth2User.getUser();
        AppointmentInfo data = appointmentService.getAppointmentDetail(user, appointmentId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("약속 상세 조회를 성공하였습니다.")
                        .data(data)
                        .build());
    }

    // 약속 페이지 조회 (나의 PICK 조회)
    // 요청자가 requester인 (확정/대여중/완료) 상태의 약속 페이지 조회
    @GetMapping
    public ResponseEntity<SuccessResponseDto> findMyAppointmentPageAsRequester(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                               @Valid FindMyAppointmentPageReq dto)
    {
        User user = oAuth2User.getUser();
        AppointmentPageRes data = appointmentService.findMyAppointmentPage(user, dto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("약속 페이지를 성공적으로 조회하였습니다.")
                        .data(data)
                        .build());
    }

    // 협의 중인 약속 수정
    @PatchMapping("/pending")
    public ResponseEntity<SuccessResponseDto> updatePendingAppointment(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                       @RequestBody @Valid UpdateAppointmentReq dto)
    {
        User user = oAuth2User.getUser();
        AppointmentRes data = appointmentService.updatePendingAppointment(user, dto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("협의 중인 약속을 성공적으로 수정하였습니다.")
                        .data(data)
                        .build());
    }

    // 확정된 약속 수정
    @PatchMapping("/confirmed")
    public ResponseEntity<SuccessResponseDto> updateConfirmedAppointment(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                         @RequestBody @Valid UpdateAppointmentReq dto)
    {
        User user = oAuth2User.getUser();
        AppointmentRes data = appointmentService.updateConfirmedAppointment(user, dto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("확정된 약속을 성공적으로 수정하였습니다.")
                        .data(data)
                        .build());
    }

    // 대여중인 약속 존재 여부 확인
    @GetMapping("/in-progress")
    public ResponseEntity<SuccessResponseDto> isInProgressAppointmentPresent(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                             @RequestParam Long chatRoomId)
    {
        User user = oAuth2User.getUser();
        boolean isPresent = appointmentService.isInProgressAppointmentPresent(user, chatRoomId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("대여중인 약속 존재 여부를 성공적으로 조회하였습니다.")
                        .data(isPresent)
                        .build());
    }
}
