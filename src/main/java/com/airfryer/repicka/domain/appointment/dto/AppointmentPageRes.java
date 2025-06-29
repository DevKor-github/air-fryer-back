package com.airfryer.repicka.domain.appointment.dto;

import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item.entity.ProductType;
import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.post.entity.PostType;
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
    private List<AppointmentInfo> appointmentInfoList;   // 약속 정보 리스트

    private PostType type;      // 타입 (대여/구매)
    private int currentPage;    // 현재 페이지 번호
    private int totalPages;     // 전체 페이지 개수

    public static AppointmentPageRes of(Map<Appointment, Optional<ItemImage>> map,
                                        PostType postType,
                                        int currentPage,
                                        int totalPages)
    {
        return AppointmentPageRes.builder()
                .appointmentInfoList(map.entrySet().stream().map(entry -> {
                    return AppointmentInfo.from(entry.getKey(), entry.getValue());
                }).toList())
                .type(postType)
                .currentPage(currentPage)
                .totalPages(totalPages)
                .build();
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    private static class AppointmentInfo
    {
        private Long appointmentId;     // 약속 ID
        private Long postId;            // 게시글 ID
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

        private static AppointmentInfo from(Appointment appointment, Optional<ItemImage> itemImage)
        {
            Post post = appointment.getPost();
            Item item = post.getItem();

            return AppointmentInfo.builder()
                    .appointmentId(appointment.getId())
                    .postId(post.getId())
                    .itemId(item.getId())
                    .requesterId(appointment.getRequester().getId())
                    .ownerId(appointment.getOwner().getId())
                    .imageUrl(itemImage.map(ItemImage::getFileKey).orElse(null))
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
}
