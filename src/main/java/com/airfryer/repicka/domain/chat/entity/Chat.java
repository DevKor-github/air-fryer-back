package com.airfryer.repicka.domain.chat.entity;

import com.airfryer.repicka.domain.appointment.entity.Appointment;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chat")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class Chat
{
    @Id
    private ObjectId id;

    // 채팅방 ID
    @NotNull
    private Long chatRoomId;

    // 사용자 ID
    @NotNull
    private Long userId;

    // 사용자 닉네임
    @NotNull
    private String nickname;

    // 내용
    @NotBlank
    private String content;

    // PICK 여부
    @NotNull
    private Boolean isPick;

    // PICK 정보
    private PickInfo pickInfo;

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    public static class PickInfo
    {
        // 약속 ID
        private Long appointmentId;

        // 요청자 ID
        private Long requesterId;

        // 제품 소유자 ID
        private Long ownerId;

        // PICK 생성자 ID
        private Long creatorId;

        // 약속 종류
        private String type;

        // 약속 상태
        private String state;

        // 대여(구매) 일시
        private LocalDateTime rentalDate;

        // 반납 일시
        private LocalDateTime returnDate;

        // 대여(구매) 장소
        private String rentalLocation;

        // 반납 장소
        private String returnLocation;

        // 대여료(판매값)
        private int price;

        // 보증금
        private int deposit;

        public static PickInfo from(Appointment appointment)
        {
            return PickInfo.builder()
                    .appointmentId(appointment.getId())
                    .requesterId(appointment.getRequester().getId())
                    .ownerId(appointment.getRequester().getId())
                    .creatorId(appointment.getRequester().getId())
                    .type(appointment.getType().name())
                    .state(appointment.getState().name())
                    .rentalDate(appointment.getRentalDate())
                    .returnDate(appointment.getReturnDate())
                    .rentalLocation(appointment.getRentalLocation())
                    .returnLocation(appointment.getReturnLocation())
                    .price(appointment.getPrice())
                    .deposit(appointment.getDeposit())
                    .build();
        }
    }
}
