package com.airfryer.repicka.domain.appointment.service;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.firebase.dto.FCMNotificationReq;
import com.airfryer.repicka.common.firebase.service.FCMService;
import com.airfryer.repicka.common.redis.RedisService;
import com.airfryer.repicka.domain.appointment.dto.UpdateAppointmentReq;
import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.appointment.entity.AppointmentType;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
import com.airfryer.repicka.domain.chat.entity.Chat;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.chat.service.ChatWebSocketService;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item.entity.TransactionType;
import com.airfryer.repicka.domain.item.repository.ItemRepository;
import com.airfryer.repicka.domain.notification.entity.NotificationType;
import com.airfryer.repicka.domain.user.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AppointmentUtil
{
    private final AppointmentRepository appointmentRepository;
    private final ItemRepository itemRepository;

    private final ChatWebSocketService chatWebSocketService;
    private final RedisService delayedQueueService;
    private final FCMService fcmService;

    // 해당 날짜에 예정된 대여 약속이 하나도 없는지 판별
    public boolean isItemAvailableOnDate(Long itemId, LocalDateTime date)
    {
        return appointmentRepository.findListOverlappingWithPeriod(
                itemId,
                List.of(AppointmentState.CONFIRMED, AppointmentState.IN_PROGRESS),
                AppointmentType.RENTAL,
                date,
                date
        ).isEmpty();
    }

    // 해당 구간 동안 예정된 대여 약속이 하나도 존재하지 않는지 판별
    public boolean isItemAvailableOnInterval(Long itemId, LocalDateTime startDate, LocalDateTime endDate)
    {
        if(endDate.isBefore(startDate)) {
            return true;
        }

        return appointmentRepository.findListOverlappingWithPeriod(
                itemId,
                List.of(AppointmentState.CONFIRMED, AppointmentState.IN_PROGRESS),
                AppointmentType.RENTAL,
                startDate,
                endDate
        ).isEmpty();
    }

    public boolean isItemAvailableOnInterval(Long itemId, LocalDateTime startDate)
    {
        return appointmentRepository.findListOverlappingWithPeriod(
                itemId,
                List.of(AppointmentState.CONFIRMED, AppointmentState.IN_PROGRESS),
                AppointmentType.RENTAL,
                startDate
        ).isEmpty();
    }

    // 제품 구매가 가능한 첫 날짜 조회
    public LocalDate getFirstSaleAvailableDate(Long itemId)
    {
        // 반환할 날짜
        LocalDate firstSaleAvailableDate = LocalDate.now();

        // 제품 데이터 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.ITEM_NOT_FOUND, itemId));

        // 대여가 가능한 경우
        if(Arrays.asList(item.getTransactionTypes()).contains(TransactionType.RENTAL))
        {
            // 예정된 대여 약속 중, 반납 날짜가 가장 늦은 약속 데이터 조회
            Optional<Appointment> appointmentOptional = appointmentRepository.findTop1ByItemIdAndStateOrderByReturnDateDesc(
                    itemId,
                    AppointmentState.CONFIRMED
            );

            // 예정된 대여 약속이 하나라도 존재하는 경우
            if(appointmentOptional.isPresent())
            {
                Appointment appointment = appointmentOptional.get();

                // 제품 구매가 가능한 첫 날짜 갱신
                if(firstSaleAvailableDate.isBefore(appointment.getReturnDate().toLocalDate().plusDays(1))) {
                    firstSaleAvailableDate = appointment.getReturnDate().toLocalDate().plusDays(1);
                }
            }
        }

        return firstSaleAvailableDate;
    }

    // 대여 구간 가능 여부 체크
    public void checkRentalPeriodPossibility(LocalDateTime startDate,
                                             LocalDateTime endDate,
                                             Item item)
    {
        // 시작 날짜가 종료 날짜 이전인지 체크
        if(!endDate.isAfter(startDate)) {
            throw new CustomException(CustomExceptionCode.RENTAL_DATE_IS_LATER_THAN_RETURN_DATE, Map.of(
                    "startDate", startDate,
                    "endDate", endDate
            ));
        }

        // 대여를 원하는 구간 동안 예정된 대여 약속이 하나도 없는지 체크
        if(!isItemAvailableOnInterval(item.getId(), startDate, endDate)) {
            throw new CustomException(CustomExceptionCode.ALREADY_RENTAL_RESERVED_PERIOD, Map.of(
                    "startDate", startDate,
                    "endDate", endDate
            ));
        }

        // 제품이 판매 예정 혹은 판매된 경우
        if(item.getSaleDate() != null)
        {
            // 대여를 원하는 구간이 판매 날짜 이전인지 체크
            if(!endDate.isBefore(item.getSaleDate())) {
                throw new CustomException(CustomExceptionCode.ALREADY_SALE_RESERVED_PERIOD, Map.of(
                        "startDate", startDate,
                        "endDate", endDate
                ));
            }
        }
    }

    // 구매 날짜 가능 여부 체크
    public void checkSaleDatePossibility(LocalDateTime saleDate, Item item)
    {
        // 판매 예정이거나 판매된 제품이 아닌지 체크
        if(item.getSaleDate() != null) {
            throw new CustomException(CustomExceptionCode.ALREADY_SALE_RESERVED, item.getId());
        }

        // 제품 구매가 가능한 첫 날짜
        LocalDate firstSaleAvailableDate = getFirstSaleAvailableDate(item.getId());

        // 구매를 원하는 날짜가 구매가 가능한 첫 날짜 이후인지 체크
        if(firstSaleAvailableDate.isAfter(saleDate.toLocalDate())) {
            throw new CustomException(CustomExceptionCode.ALREADY_RENTAL_RESERVED_PERIOD, Map.of(
                    "requestSaleDate", saleDate,
                    "firstAvailableSaleDate", firstSaleAvailableDate
            ));
        }
    }

    // 약속 수정 가능 여부 체크
    public void checkUpdateAppointmentPossibility(Appointment appointment,
                                                  User user,
                                                  UpdateAppointmentReq dto)
    {
        // 약속 상태 체크
        if(appointment.getState() != AppointmentState.PENDING && appointment.getState() != AppointmentState.CONFIRMED) {
            throw new CustomException(CustomExceptionCode.CONFLICT_APPOINTMENT_STATE, appointment.getState());
        }

        // 요청자가 약속 관계자인지 체크
        if(!Objects.equals(user.getId(), appointment.getOwner().getId()) && !Objects.equals(user.getId(), appointment.getRequester().getId())) {
            throw new CustomException(CustomExceptionCode.NOT_APPOINTMENT_PARTICIPANT, null);
        }

        // 제품 삭제 여부 확인
        if(appointment.getItem().getIsDeleted()) {
            throw new CustomException(CustomExceptionCode.ALREADY_DELETED_ITEM, null);
        }

        // 가격 협의가 불가능한데 가격을 바꿔서 요청을 보내는 경우, 예외 처리
        if(!appointment.getItem().getCanDeal() && (dto.getPrice() != appointment.getPrice() || dto.getDeposit() != appointment.getDeposit())) {
            throw new CustomException(CustomExceptionCode.DEAL_NOT_ALLOWED, null);
        }

        // 대여 게시글의 경우
        if(appointment.getType() == AppointmentType.RENTAL)
        {
            // 반납 일시와 반납 장소 정보가 존재하는지 확인
            if(dto.getRentalDate() == null || dto.getRentalLocation() == null || dto.getDeposit() == null) {
                throw new CustomException(CustomExceptionCode.INVALID_RENTAL_INFORMATION, null);
            }

            // 대여 구간 가능 여부 체크
            checkRentalPeriodPossibility(dto.getRentalDate(), dto.getReturnDate(), appointment.getItem());
        }
        // 판매 게시글의 경우
        else
        {
            // 구매 날짜 가능 여부 체크
            checkSaleDatePossibility(dto.getRentalDate(), appointment.getItem());
        }
    }

    // 완료되지 않은 약속 취소
    @Transactional
    public void cancelCurrentAppointment(ChatRoom chatRoom, User user)
    {
        // 채팅 상대방 조회

        User opponent = Objects.equals(chatRoom.getRequester().getId(), user.getId()) ? chatRoom.getOwner() : chatRoom.getRequester();

        // 완료되지 않은 약속 조회
        List<Appointment> currentAppointmentOptional = appointmentRepository.findByItemIdAndOwnerIdAndRequesterIdAndStateIn(
                chatRoom.getItem().getId(),
                chatRoom.getOwner().getId(),
                chatRoom.getRequester().getId(),
                List.of(AppointmentState.PENDING, AppointmentState.CONFIRMED, AppointmentState.IN_PROGRESS)
        );

        // 완료되지 않은 약속이 존재하는 경우
        if(!currentAppointmentOptional.isEmpty())
        {
            // 완료되지 않은 약속
            Appointment currentAppointment = currentAppointmentOptional.getFirst();

            // 완료되지 않은 약속이 대여 중 상태인 경우, 예외 처리
            if(currentAppointment.getState().equals(AppointmentState.IN_PROGRESS)) {
                throw new CustomException(CustomExceptionCode.IN_PROGRESS_APPOINTMENT_EXIST, null);
            }

            /// 약속 취소 처리

            // 제품의 판매 예정 날짜 초기화
            currentAppointment.getItem().cancelSale();

            // 약속 취소
            currentAppointment.cancel();

            // 약속 알림 발송 예약 취소
            delayedQueueService.cancelDelayedTask("appointment", currentAppointment.getId());

            cancelAppointment(currentAppointment);

            /// 약속 취소 채팅 및 알림 전송

            // 채팅 생성
            Chat cancelChat = Chat.builder()
                    .chatRoomId(chatRoom.getId())
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .content(user.getNickname() + " 님께서 약속을 취소하였습니다.")
                    .isNotification(true)
                    .isPick(false)
                    .pickInfo(null)
                    .build();

            // 채팅 전송
            chatWebSocketService.sendMessageChat(user, chatRoom, cancelChat);

            // 푸시 알림 전송
            FCMNotificationReq cancelNotificationReq = FCMNotificationReq.of(NotificationType.APPOINTMENT_CANCEL, currentAppointment.getId().toString(), user.getNickname());
            fcmService.sendNotification(opponent.getFcmToken(), cancelNotificationReq);
        }
    }

    // 약속 취소
    @Transactional
    public void cancelAppointment(Appointment appointment)
    {
        // 약속 상태 변경
        appointment.cancel();

        // 약속 알림 발송 예약 취소
        delayedQueueService.cancelDelayedTask("appointment", appointment.getId());

        // 제품의 판매 예정 날짜 초기화
        appointment.getItem().cancelSale();
    }
}
