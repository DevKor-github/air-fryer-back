package com.airfryer.repicka.domain.appointment.dto;

import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.appointment.entity.AppointmentType;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item.entity.ProductType;
import com.airfryer.repicka.domain.item.entity.PostType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class AppointmentPageRes
{
    private AppointmentType type;   // 타입 (대여/구매)

    private List<AppointmentInfo> appointmentInfoList;  // 약속 정보 리스트
    private PageInfo pageInfo;                          // 페이지 정보

    public static AppointmentPageRes of(Map<Appointment, Optional<String>> map,
                                        AppointmentType type,
                                        AppointmentState cursorState,
                                        LocalDateTime cursorDate,
                                        Long cursorId,
                                        Boolean hasNext)
    {
        return AppointmentPageRes.builder()
                .appointmentInfoList(map.entrySet().stream().map(entry -> {
                    return AppointmentInfo.from(entry.getKey(), entry.getValue());
                }).toList())
                .type(type)
                .pageInfo(PageInfo.builder()
                        .cursorState(cursorState)
                        .cursorDate(cursorDate)
                        .cursorId(cursorId)
                        .hasNext(hasNext)
                        .build())
                .build();
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    private static class AppointmentInfo
    {
        private Long appointmentId;     // 약속 ID
        private Long itemId;            // 제품 ID
        private Long requesterId;       // 대여자(구매자) ID
        private Long ownerId;           // 소유자 ID

        private String imageUrl;            // 이미지 URL
        private String title;               // 게시글 제목
        private String description;         // 게시글 설명
        private ProductType[] productTypes; // 제품 타입
        private LocalDateTime rentalDate;   // 대여(구매) 일시
        private LocalDateTime returnDate;   // 반납 일시
        private String rentalLocation;      // 대여(구매) 장소
        private String returnLocation;      // 반납 장소
        private int price;                  // 대여료(판매값)
        private int deposit;                // 보증금
        private AppointmentState state;     // 약속 상태

        private static AppointmentInfo from(Appointment appointment, Optional<String> imageUrl)
        {
            Item item = appointment.getItem();

            return AppointmentInfo.builder()
                    .appointmentId(appointment.getId())
                    .itemId(item.getId())
                    .requesterId(appointment.getRequester().getId())
                    .ownerId(appointment.getOwner().getId())
                    .imageUrl(imageUrl.orElse(null))
                    .title(item.getTitle())
                    .description(item.getDescription())
                    .productTypes(item.getProductTypes())
                    .rentalDate(appointment.getRentalDate())
                    .returnDate(appointment.getReturnDate())
                    .rentalLocation(appointment.getRentalLocation())
                    .returnLocation(appointment.getReturnLocation())
                    .price(appointment.getPrice())
                    .deposit(appointment.getDeposit())
                    .state(appointment.getState())
                    .build();
        }
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    private static class PageInfo
    {
        // 커서
        private AppointmentState cursorState;   // 약속 상태
        private LocalDateTime cursorDate;       // 대여(구매) 일시
        private Long cursorId;                  // 약속 ID

        private Boolean hasNext;    // 다음 페이지가 존재하는가?
    }
}
