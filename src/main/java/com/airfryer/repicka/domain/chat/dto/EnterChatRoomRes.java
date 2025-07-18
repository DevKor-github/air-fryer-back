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

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class EnterChatRoomRes
{
    private ChatRoomInfoDto chatRoom;           // 채팅방 정보
    private List<ChatInfo> chats;               // 채팅 정보 리스트
    private ItemInfo item;                      // 제품 정보

    private ObjectId chatCursorId;              // 채팅: 커서 ID
    private Boolean chatHasNext;                // 채팅: 다음 페이지가 존재하는가?

    public static EnterChatRoomRes of(ChatRoom chatRoom,
                                      User me,
                                      String imageUrl,
                                      List<Chat> chatList,
                                      ObjectId chatCursorId,
                                      boolean chatHasNext)
    {
        return EnterChatRoomRes.builder()
                .chatRoom(ChatRoomInfoDto.from(chatRoom, me))
                .item(ItemInfo.from(chatRoom.getItem(), imageUrl))
                .chats(chatList.stream().map(ChatInfo::from).toList())
                .chatCursorId(chatCursorId)
                .chatHasNext(chatHasNext)
                .build();
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
}
