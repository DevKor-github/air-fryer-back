package com.airfryer.repicka.domain.appointment.controller;

import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.appointment.dto.AppointmentRes;
import com.airfryer.repicka.domain.appointment.dto.OfferToUpdateInProgressAppointmentReq;
import com.airfryer.repicka.domain.appointment.dto.UpdateInProgressAppointmentRes;
import com.airfryer.repicka.domain.appointment.service.UpdateInProgressAppointmentService;
import com.airfryer.repicka.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/update-in-progress-appointment")
@RequiredArgsConstructor
public class UpdateInProgressAppointmentController
{
    private final UpdateInProgressAppointmentService updateInProgressAppointmentService;

    // 대여 중인 약속 변경 제시
    @PostMapping()
    public ResponseEntity<SuccessResponseDto> offerToUpdateInProgressAppointment(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                                 @RequestBody @Valid OfferToUpdateInProgressAppointmentReq dto)
    {
        User user = oAuth2User.getUser();
        AppointmentRes data = updateInProgressAppointmentService.offerToUpdateInProgressAppointment(user, dto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("대여 중인 약속의 변경을 성공적으로 제시하였습니다.")
                        .data(data)
                        .build());
    }

    // 대여 중인 약속 변경 제시 데이터 조회
    @GetMapping()
    public ResponseEntity<SuccessResponseDto> findOfferToUpdateInProgressAppointment(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                                     @RequestParam Long appointmentId,
                                                                                     @RequestParam Boolean isMine)
    {
        User user = oAuth2User.getUser();
        UpdateInProgressAppointmentRes data = updateInProgressAppointmentService.findOfferToUpdateInProgressAppointment(user, appointmentId, isMine);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("대여 중인 약속 변경 제시 데이터를 성공적으로 조회하였습니다.")
                        .data(data)
                        .build());
    }

    // 대여 중인 약속 변경 제시 수락 및 거절
    @PatchMapping("/{updateInProgressAppointmentId}")
    public ResponseEntity<SuccessResponseDto> acceptOfferToUpdateInProgressAppointment(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                                       @PathVariable Long updateInProgressAppointmentId,
                                                                                       @RequestParam Boolean isAccepted)
    {
        User user = oAuth2User.getUser();
        updateInProgressAppointmentService.responseOfferToUpdateInProgressAppointment(user, updateInProgressAppointmentId, isAccepted);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("대여 중인 약속 변경 제시를 성공적으로 " + (isAccepted ? "수락" : "거절") + "하였습니다.")
                        .data(null)
                        .build());
    }

    // 대여 중인 약속 변경 제시 취소
    @DeleteMapping("/{updateInProgressAppointmentId}")
    public ResponseEntity<SuccessResponseDto> deleteOfferToUpdateInProgressAppointment(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                                       @PathVariable Long updateInProgressAppointmentId)
    {
        User user = oAuth2User.getUser();
        updateInProgressAppointmentService.deleteOfferToUpdateInProgressAppointment(user, updateInProgressAppointmentId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("대여 중인 약속 변경 제시를 성공적으로 취소하였습니다.")
                        .data(null)
                        .build());
    }
}
