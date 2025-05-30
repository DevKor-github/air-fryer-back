package com.airfryer.repicka.domain.appointment.controller;

import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.appointment.dto.GetItemAvailabilityRes;
import com.airfryer.repicka.domain.appointment.dto.OfferAppointmentInPostReq;
import com.airfryer.repicka.domain.appointment.service.AppointmentService;
import com.airfryer.repicka.domain.user.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AppointmentController
{
    private final AppointmentService appointmentService;

    // 게시글에서 약속 제시
    @PostMapping("/appointment/in-post")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> offerAppointmentInPost(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                     @RequestBody @Valid OfferAppointmentInPostReq dto)
    {
        User borrower = oAuth2User.getUser();
        appointmentService.offerAppointmentInPost(borrower, dto);

        // TODO: 채팅방 데이터를 data로 응답해야 함.
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponseDto.builder()
                        .message("게시글에서 약속을 성공적으로 제시하였습니다.")
                        .data(null)
                        .build());
    }

    // TODO: 채팅방이 구현되면, 채팅방에서 약속 제시 API 구현
    /*
        // 채팅방에서 약속 제시
        @PostMapping("/appointment/in-chat-room")
        @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
        public ResponseEntity<SuccessResponseDto> offerAppointmentInChatRoom(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                             @RequestBody @Valid OfferAppointmentInChatRoomReq dto)
        {
            User requester = oAuth2User.getUser();
            appointmentService.offerAppointmentInChatRoom(requester, dto);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(SuccessResponseDto.builder()
                            .message("채팅방에서 약속을 성공적으로 제시하였습니다.")
                            .data(null)
                            .build());
        }
    */

    // 월 단위로 날짜별 제품 대여 가능 여부 조회
    @GetMapping("/post/{postId}/availability")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<SuccessResponseDto> getItemAvailability(@PathVariable Long postId,
                                                                  @RequestParam int year,
                                                                  @RequestParam int month)
    {
        GetItemAvailabilityRes data = appointmentService.getItemAvailability(postId, year, month);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("날짜별 제품 대여 가능 여부를 성공적으로 조히하였습니다.")
                        .data(data)
                        .build());
    }
}
