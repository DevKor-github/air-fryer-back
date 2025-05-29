package com.airfryer.repicka.domain.appointment.controller;

import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.appointment.dto.CreateAppointmentInPostReq;
import com.airfryer.repicka.domain.appointment.service.AppointmentService;
import com.airfryer.repicka.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AppointmentController
{
    private final AppointmentService appointmentService;

    // 대여 신청을 통한 약속 제시
    // 빌리고 싶은 사람이 게시글에서 바로 대여 신청 버튼을 눌러서 약속을 제시하는 방식
    @PostMapping("/post/{postId}/appointment")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> offerAppointmentInPost(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                      @PathVariable Long postId,
                                                                      @RequestBody @Valid CreateAppointmentInPostReq dto)
    {
        User borrower = oAuth2User.getUser();
        appointmentService.offerAppointmentInPost(borrower, postId, dto);

        // TODO: 채팅방을 새로 생성하였다면 data로 데이터 응답해야 함.
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .message("약속을 성공적으로 생성하였습니다.")
                        .data(null)
                        .build());
    }
}
