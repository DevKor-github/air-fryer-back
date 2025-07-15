package com.airfryer.repicka.domain.chat.dto;

import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.appointment.entity.AppointmentType;
import com.airfryer.repicka.domain.chat.entity.Chat;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.item.entity.*;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class EnterChatRoomRes
{
    private ChatRoomInfo chatRoom;              // 채팅방 정보
    private ItemInfo item;                      // 제품 정보
    private List<ChatInfo> chats;               // 채팅 정보 리스트
    private List<AppointmentInfo> appointment;  // 약속 정보 리스트

    private ObjectId chatCursorId;              // 채팅: 커서 ID
    private Boolean chatHasNext;                // 채팅: 다음 페이지가 존재하는가?

    public static EnterChatRoomRes of(ChatRoom chatRoom,
                                      User me,
                                      String imageUrl,
                                      List<Chat> chatList,
                                      List<Appointment> appointmentList,
                                      ObjectId chatCursorId,
                                      boolean chatHasNext)
    {
        return EnterChatRoomRes.builder()
                .chatRoom(ChatRoomInfo.from(chatRoom, me))
                .item(ItemInfo.from(chatRoom.getItem(), imageUrl))
                .chats(chatList.stream().map(ChatInfo::from).toList())
                .appointment(appointmentList.stream().map(AppointmentInfo::from).toList())
                .chatCursorId(chatCursorId)
                .chatHasNext(chatHasNext)
                .build();
    }

    // 채팅방 정보
    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    private static class ChatRoomInfo
    {
        private Long chatRoomId;            // 채팅방 ID
        private Long myUserId;              // 나의 사용자 ID
        private Long opponentUserId;        // 상대방의 사용자 ID
        private Boolean isOpponentKorean;   // 상대방의 고려대 인증 여부
        private Boolean isFinished;         // 채팅방 종료 여부

        private static ChatRoomInfo from(ChatRoom chatRoom, User me)
        {
            User opponent = Objects.equals(chatRoom.getRequester().getId(), me.getId()) ? chatRoom.getOwner() : chatRoom.getRequester();

            return ChatRoomInfo.builder()
                    .chatRoomId(chatRoom.getId())
                    .myUserId(me.getId())
                    .opponentUserId(opponent.getId())
                    .isOpponentKorean(opponent.getIsKoreaUnivVerified())
                    .isFinished(chatRoom.getIsFinished())
                    .build();
        }
    }

    // 제품 정보
    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    private static class ItemInfo
    {
        private Long itemId;                        // 제품 ID
        private String imageUrl;                    // 이미지 URL
        private ProductType[] productTypes;         // 제품 타입
        private TransactionType[] transactionTypes; // 거래 타입
        private String title;                       // 제목
        private String description;                 // 설명
        private ItemColor color;                    // 색상
        private ItemSize size;                      // 사이즈
        private ItemQuality quality;                // 품질
        private int rentalFee;                      // 대여료
        private int salePrice;                      // 판매값
        private int deposit;                        // 보증금
        private String location;                    // 기본 장소
        private TradeMethod[] tradeMethods;         // 거래 방식
        private Boolean canDeal;                    // 가격 제시 가능 여부
        private LocalDateTime saleDate;             // 판매 예정 날짜

        private static ItemInfo from(Item item, String imageUrl)
        {
            return ItemInfo.builder()
                    .itemId(item.getId())
                    .imageUrl(imageUrl)
                    .productTypes(item.getProductTypes())
                    .transactionTypes(item.getTransactionTypes())
                    .title(item.getTitle())
                    .description(item.getDescription())
                    .color(item.getColor())
                    .size(item.getSize())
                    .quality(item.getQuality())
                    .rentalFee(item.getRentalFee())
                    .salePrice(item.getSalePrice())
                    .deposit(item.getDeposit())
                    .location(item.getLocation())
                    .tradeMethods(item.getTradeMethods())
                    .canDeal(item.getCanDeal())
                    .saleDate(item.getSaleDate())
                    .build();
        }
    }

    // 채팅 정보
    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    private static class ChatInfo
    {
        private ObjectId chatId;    // 채팅 ID
        private Long userId;        // 사용자 ID
        private String content;     // 내용

        private static ChatInfo from(Chat chat)
        {
            return ChatInfo.builder()
                    .chatId(chat.getId())
                    .userId(chat.getUserId())
                    .content(chat.getContent())
                    .build();
        }
    }

    // 약속 정보
    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    private static class AppointmentInfo
    {
        private Long appointmentId;         // 약속 ID
        private AppointmentType type;       // 약속 타입
        private AppointmentState state;     // 약속 상태
        private LocalDateTime rentalDate;   // 대여(구매) 일시
        private LocalDateTime returnDate;   // 반납 일시
        private String rentalLocation;      // 대여(구매) 장소
        private String returnLocation;      // 반납 장소
        private int price;                  // 대여료(판매값)
        private int deposit;                // 보증금

        private static AppointmentInfo from(Appointment appointment)
        {
            return AppointmentInfo.builder()
                    .appointmentId(appointment.getId())
                    .type(appointment.getType())
                    .state(appointment.getState())
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
