package com.airfryer.repicka.domain.appointment.service;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.firebase.dto.FCMNotificationReq;
import com.airfryer.repicka.common.firebase.type.NotificationType;
import com.airfryer.repicka.common.redis.RedisService;
import com.airfryer.repicka.common.firebase.service.FCMService;
import com.airfryer.repicka.common.redis.dto.AppointmentTask;
import com.airfryer.repicka.common.redis.type.TaskType;
import com.airfryer.repicka.domain.appointment.FindMyAppointmentSubject;
import com.airfryer.repicka.domain.appointment.dto.*;
import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.appointment.entity.AppointmentType;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
import com.airfryer.repicka.domain.chat.dto.EnterChatRoomRes;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.chat.repository.ChatRoomRepository;
import com.airfryer.repicka.domain.chat.service.ChatService;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item.repository.ItemRepository;
import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import com.airfryer.repicka.domain.item_image.repository.ItemImageRepository;
import com.airfryer.repicka.domain.item.entity.TransactionType;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService
{
    private final AppointmentRepository appointmentRepository;
    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;
    private final ChatRoomRepository chatRoomRepository;

    private final ChatService chatService;
    private final RedisService delayedQueueService;
    private final FCMService fcmService;

    /// 서비스

    // 대여 약속 제시
    @Transactional
    public EnterChatRoomRes offerRentalAppointment(User borrower, OfferRentalAppointmentReq dto)
    {
        /// 제품 데이터 조회

        // 제품 데이터 조회
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.ITEM_NOT_FOUND, dto.getItemId()));

        // 제품 삭제 여부 확인
        if(item.getIsDeleted()) {
            throw new CustomException(CustomExceptionCode.ALREADY_DELETED_ITEM, null);
        }

        // 대여가 가능한 제품인지 확인
        if(!Arrays.asList(item.getTransactionTypes()).contains(TransactionType.RENTAL)) {
            throw new CustomException(CustomExceptionCode.CANNOT_RENTAL_ITEM, null);
        }

        // 가격 협의가 불가능한데 가격을 바꾸지는 않았는지 체크
        if(!item.getCanDeal() && (dto.getRentalFee() != item.getRentalFee() || dto.getDeposit() != item.getDeposit())) {
            throw new CustomException(CustomExceptionCode.DEAL_NOT_ALLOWED, null);
        }

        // 대여 구간 가능 여부 체크
        checkRentalPeriodPossibility(dto.getRentalDate(), dto.getReturnDate(), item);

        // 완료되지 않은 약속 데이터가 존재하지 않는지 체크
        List<Appointment> currentAppointmentOptional = appointmentRepository.findByItemIdAndOwnerIdAndRequesterIdAndStateIn(
                item.getId(),
                item.getOwner().getId(),
                borrower.getId(),
                List.of(AppointmentState.PENDING, AppointmentState.CONFIRMED, AppointmentState.IN_PROGRESS)
        );

        if(!currentAppointmentOptional.isEmpty()) {
            throw new CustomException(CustomExceptionCode.CURRENT_APPOINTMENT_EXIST, null);
        }

        /// 새로운 약속 데이터 생성

        // 새로운 약속 데이터
        Appointment appointment = Appointment.builder()
                .item(item)
                .requester(borrower)
                .owner(item.getOwner())
                .creator(borrower)
                .type(AppointmentType.RENTAL)
                .state(AppointmentState.PENDING)
                .rentalDate(dto.getRentalDate())
                .returnDate(dto.getReturnDate())
                .rentalLocation(dto.getRentalLocation().trim())
                .returnLocation(dto.getReturnLocation().trim())
                .price(dto.getRentalFee())
                .deposit(dto.getDeposit())
                .build();

        // 약속 데이터 저장
        appointmentRepository.save(appointment);

        /// 채팅방 조회 (존재하지 않으면 생성)

        ChatRoom chatRoom = chatService.createChatRoom(item, borrower);

        /// 제품 소유자에게 약속 제시 알림

        FCMNotificationReq notificationReq = FCMNotificationReq.of(NotificationType.APPOINTMENT_PROPOSAL, appointment.getId().toString(), item.getOwner().getNickname());
        fcmService.sendNotification(item.getOwner().getFcmToken(), notificationReq);

        // TODO: PICK 메시지 전송

        /// 채팅방 입장 데이터 반환

        return chatService.enterChatRoom(borrower, chatRoom, 1);
    }

    // 구매 약속 제시
    @Transactional
    public EnterChatRoomRes offerSaleAppointment(User buyer, OfferSaleAppointmentReq dto)
    {
        /// 제품 데이터 조회

        // 제품 데이터 조회
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.ITEM_NOT_FOUND, dto.getItemId()));

        // 제품 삭제 여부 확인
        if(item.getIsDeleted()) {
            throw new CustomException(CustomExceptionCode.ALREADY_DELETED_ITEM, null);
        }

        // 구매가 가능한 제품인지 확인
        if(!Arrays.asList(item.getTransactionTypes()).contains(TransactionType.SALE)) {
            throw new CustomException(CustomExceptionCode.CANNOT_SALE_ITEM, null);
        }

        // 가격 협의가 불가능한데 가격을 바꾸지는 않았는지 체크
        if(!item.getCanDeal() && (dto.getSalePrice() != item.getSalePrice())) {
            throw new CustomException(CustomExceptionCode.DEAL_NOT_ALLOWED, null);
        }

        // 구매 날짜 가능 여부 체크
        checkSaleDatePossibility(dto.getSaleDate(), item);

        // 완료되지 않은 약속 데이터가 존재하지 않는지 체크
        List<Appointment> currentAppointmentOptional = appointmentRepository.findByItemIdAndOwnerIdAndRequesterIdAndStateIn(
                item.getId(),
                item.getOwner().getId(),
                buyer.getId(),
                List.of(AppointmentState.PENDING, AppointmentState.CONFIRMED, AppointmentState.IN_PROGRESS)
        );

        if(!currentAppointmentOptional.isEmpty()) {
            throw new CustomException(CustomExceptionCode.CURRENT_APPOINTMENT_EXIST, null);
        }

        /// 새로운 약속 데이터 생성

        // 새로운 약속 데이터
        Appointment appointment = Appointment.builder()
                .item(item)
                .requester(buyer)
                .owner(item.getOwner())
                .creator(buyer)
                .type(AppointmentType.SALE)
                .state(AppointmentState.PENDING)
                .rentalDate(dto.getSaleDate())
                .returnDate(null)
                .rentalLocation(dto.getSaleLocation().trim())
                .returnLocation(null)
                .price(dto.getSalePrice())
                .deposit(0)
                .build();

        // 약속 데이터 저장
        appointmentRepository.save(appointment);

        /// 채팅방 조회 (존재하지 않으면 생성)

        ChatRoom chatRoom = chatService.createChatRoom(item, buyer);

        /// 제품 소유자에게 약속 제시 알림

        FCMNotificationReq notificationReq = FCMNotificationReq.of(NotificationType.APPOINTMENT_PROPOSAL, appointment.getId().toString(), item.getOwner().getNickname());
        fcmService.sendNotification(item.getOwner().getFcmToken(), notificationReq);

        // TODO: PICK 메시지 전송

        /// 채팅방 입장 데이터 반환

        return chatService.enterChatRoom(buyer, chatRoom, 1);
    }

    // 약속 확정
    @Transactional
    public AppointmentRes confirmAppointment(User user, Long appointmentId)
    {
        /// 약속 데이터 조회

        // 약속 데이터 조회
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.APPOINTMENT_NOT_FOUND, appointmentId));

        // 협의 중인 약속인지 체크
        if(appointment.getState() != AppointmentState.PENDING) {
            throw new CustomException(CustomExceptionCode.CONFLICT_APPOINTMENT_STATE, appointment.getState());
        }

        // 동의하는 사용자가 약속을 생성한 사용자와 다른지 체크
        if(Objects.equals(user.getId(), appointment.getCreator().getId())) {
            throw new CustomException(CustomExceptionCode.CANNOT_CONFIRM_APPOINTMENT_MYSELF, null);
        }

        // 동의자가 약속 관계자인지 체크
        if(!Objects.equals(user.getId(), appointment.getOwner().getId()) && !Objects.equals(user.getId(), appointment.getRequester().getId())) {
            throw new CustomException(CustomExceptionCode.NOT_APPOINTMENT_PARTICIPANT, null);
        }

        /// 제품 데이터 조회

        // 제품 데이터 조회
        Item item = appointment.getItem();

        // 제품 삭제 여부 확인
        if(item.getIsDeleted()) {
            throw new CustomException(CustomExceptionCode.ALREADY_DELETED_ITEM, null);
        }

        // 대여 약속의 경우
        if(appointment.getType() == AppointmentType.RENTAL)
        {
            // 대여 구간 가능 여부 체크
            checkRentalPeriodPossibility(
                    appointment.getRentalDate(),
                    appointment.getReturnDate(),
                    item
            );
        }
        // 구매 약속의 경우
        else
        {
            // 구매 날짜 가능 여부 체크
            checkSaleDatePossibility(appointment.getRentalDate(), item);

            // 제품의 판매 예정 날짜 변경
            item.confirmSale(LocalDateTime.now());
        }

        // 약속 상태 변경
        appointment.confirmAppointment();

        // 약속 확정 알림
        FCMNotificationReq notificationReq = FCMNotificationReq.of(NotificationType.APPOINTMENT_CONFIRMATION, appointment.getId().toString(), appointment.getItem().getTitle());
        fcmService.sendNotification(appointment.getCreator().getFcmToken(), notificationReq);

        // 약속 알림 발송 예약
        delayedQueueService.addDelayedTask(
                "appointment",
                AppointmentTask.from(appointment, TaskType.REMIND),
                appointment.getRentalDate().minusDays(1)
        );

        // 약속 데이터 반환
        return AppointmentRes.from(appointment);
    }

    // 약속 취소
    @Transactional
    public AppointmentRes cancelAppointment(User user, Long appointmentId)
    {
        /// 약속 데이터 조회

        // 약속 데이터 조회
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.APPOINTMENT_NOT_FOUND, appointmentId));

        // 협의 중이거나 확정된 약속인지 체크
        if(appointment.getState() != AppointmentState.PENDING && appointment.getState() != AppointmentState.CONFIRMED) {
            throw new CustomException(CustomExceptionCode.APPOINTMENT_CANNOT_CANCELLED, appointment.getState());
        }

        // 취소자가 약속 관계자인지 체크
        if(!Objects.equals(user.getId(), appointment.getOwner().getId()) && !Objects.equals(user.getId(), appointment.getRequester().getId())) {
            throw new CustomException(CustomExceptionCode.NOT_APPOINTMENT_PARTICIPANT, null);
        }

        /// 제품 데이터 조회

        // 제품 데이터 조회
        Item item = appointment.getItem();

        // 구매 약속의 경우
        if(appointment.getType() == AppointmentType.SALE)
        {
            // 제품의 판매 예정 날짜 변경
            item.cancelSale();
        }

        /// 약속 상태 변경

        appointment.cancelAppointment();

        // 약속 알림 발송 예약 취소
        delayedQueueService.cancelDelayedTask("appointment", appointmentId);

        // TODO: 사용자 피드백 요청

        // 약속 데이터 반환
        return AppointmentRes.from(appointment);
    }

    // (확정/대여중/완료) 상태의 나의 약속 페이지 조회
    @Transactional(readOnly = true)
    public AppointmentPageRes findMyAppointmentPage(User user,
                                                    FindMyAppointmentSubject subject,
                                                    FindMyAppointmentPageReq dto)
    {
        /// 약속 페이지 조회

        List<Appointment> appointmentPage = subject.findAppointmentPage(
                appointmentRepository,
                user,
                dto
        );

        /// 페이지 정보 계산

        // 다음 페이지가 존재하는가?
        Boolean hasNext = appointmentPage.size() > dto.getPageSize();

        // 반환할 커서 데이터
        AppointmentState cursorState = hasNext ? appointmentPage.getLast().getState() : null;
        LocalDateTime cursorDate = hasNext ? appointmentPage.getLast().getRentalDate() : null;
        Long cursorId = hasNext ? appointmentPage.getLast().getId() : null;

        // 다음 페이지가 존재한다면, 마지막 아이템 제거
        if(hasNext) {
            appointmentPage = appointmentPage.subList(0, dto.getPageSize());
        }

        /// 대표 이미지 리스트 조회

        // 대표 이미지 리스트 조회
        List<ItemImage> thumbnailList = itemImageRepository.findThumbnailListByItemIdList(appointmentPage.stream().map(appointment -> appointment.getItem().getId()).toList());

        // Map(제품 ID, 대표 이미지 URL) 생성
        Map<Long, String> thumbnailUrlMap = thumbnailList.stream()
                .collect(Collectors.toMap(
                        itemImage -> itemImage.getItem().getId(),
                        ItemImage::getFileKey
                ));

        // Map(약속, 대표 이미지 URL) 생성
        Map<Appointment, Optional<String>> map = appointmentPage.stream()
                .collect(Collectors.toMap(
                        appointment -> appointment,
                        appointment -> Optional.ofNullable(thumbnailUrlMap.get(appointment.getItem().getId())),
                        (a, b) -> b,
                        LinkedHashMap::new
                ));

        /// 데이터 반환

        return AppointmentPageRes.of(
                map,
                dto.getType(),
                cursorState,
                cursorDate,
                cursorId,
                hasNext
        );
    }

    // 완료되지 않은 약속 조회
    @Transactional(readOnly = true)
    public CurrentAppointmentRes findCurrentAppointment(User requester, Long itemId)
    {
        /// 제품 조회

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.ITEM_NOT_FOUND, itemId));

        // 제품 삭제 여부 확인
        if(item.getIsDeleted()) {
            throw new CustomException(CustomExceptionCode.ALREADY_DELETED_ITEM, null);
        }

        // 요청자와 제품 소유자가 다른 사용자인지 체크
        if(Objects.equals(item.getOwner().getId(), requester.getId())) {
            throw new CustomException(CustomExceptionCode.SAME_OWNER_AND_REQUESTER, null);
        }

        /// 완료되지 않은 약속 조회

        List<Appointment> currentAppointmentOptional = appointmentRepository.findByItemIdAndOwnerIdAndRequesterIdAndStateIn(
                item.getId(),
                item.getOwner().getId(),
                requester.getId(),
                List.of(AppointmentState.PENDING, AppointmentState.CONFIRMED, AppointmentState.IN_PROGRESS)
        );

        /// 데이터 반환

        if(currentAppointmentOptional.isEmpty())
        {
            return CurrentAppointmentRes.from(false, null, null);
        }
        else
        {
            // 완료되지 않은 약속
            Appointment currentAppointment = currentAppointmentOptional.getFirst();

            // 채팅방 조회
            Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findByItemIdAndOwnerIdAndRequesterId(item.getId(), item.getOwner().getId(), requester.getId());

            if(chatRoomOptional.isEmpty()) {
                throw new CustomException(CustomExceptionCode.CHATROOM_NOT_FOUND, null);
            }

            ChatRoom chatRoom = chatRoomOptional.get();

            return CurrentAppointmentRes.from(true, currentAppointment, chatRoom);
        }
    }

    // 협의 중인 약속 수정
    @Transactional
    public AppointmentRes updatePendingAppointment(User user, UpdateAppointmentReq dto)
    {
        /// 약속 데이터 조회

        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.APPOINTMENT_NOT_FOUND, dto.getAppointmentId()));

        /// 제품 데이터 조회

        Item item = appointment.getItem();

        /// 약속 수정이 가능한지 체크

        checkUpdateAppointmentPossibility(
                appointment,
                item,
                user,
                dto,
                AppointmentState.PENDING
        );

        /// 약속 변경

        // 약속 데이터 변경
        appointment.updateAppointment(user, dto, appointment.getType() == AppointmentType.RENTAL);

        // 약속 데이터 반환
        return AppointmentRes.from(appointment);
    }

    // 확정된 약속 수정
    @Transactional
    public AppointmentRes updateConfirmedAppointment(User user, UpdateAppointmentReq dto)
    {
        /// 약속 데이터 조회

        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.APPOINTMENT_NOT_FOUND, dto.getAppointmentId()));

        /// 제품 데이터 조회

        Item item = appointment.getItem();

        /// 약속 수정이 가능한지 체크

        checkUpdateAppointmentPossibility(
                appointment,
                item,
                user,
                dto,
                AppointmentState.CONFIRMED
        );

        /// 기존 약속 취소

        // 약속 취소
        appointment.cancelAppointment();

        // 약속 알림 발송 예약 취소
        delayedQueueService.cancelDelayedTask("appointment", appointment.getId());

        // 제품의 판매 예정 날짜 초기화
        item.cancelSale();

        /// 새로운 약속 생성

        // 새로운 약속 데이터 생성
        Appointment newAppointment = appointment.clone();
        newAppointment.updateAppointment(user, dto, appointment.getType() == AppointmentType.RENTAL);

        // 약속 데이터 저장
        appointmentRepository.save(newAppointment);

        // 약속 데이터 반환
        return AppointmentRes.from(newAppointment);
    }

    /// ============================ 공통 로직 ============================

    /// 해당 날짜에 예정된 대여 약속이 하나도 없는지 판별

    public boolean isItemAvailableOnDate(Long itemId, LocalDateTime date) {
        return appointmentRepository.findListOverlappingWithPeriod(
                itemId,
                List.of(AppointmentState.CONFIRMED, AppointmentState.IN_PROGRESS),
                AppointmentType.RENTAL,
                date,
                date
        ).isEmpty();
    }

    /// 해당 구간 동안 예정된 대여 약속이 하나도 존재하지 않는지 판별

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

    /// 제품 구매가 가능한 첫 날짜 조회

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

    /// 대여 구간 가능 여부 체크

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

    /// 구매 날짜 가능 여부 체크

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

    /// 약속 수정 가능 여부 체크

    private void checkUpdateAppointmentPossibility(Appointment appointment,
                                                   Item item,
                                                   User user,
                                                   UpdateAppointmentReq dto,
                                                   AppointmentState state)
    {
        // 약속 상태 체크
        if(appointment.getState() != state) {
            throw new CustomException(CustomExceptionCode.CONFLICT_APPOINTMENT_STATE, appointment.getState());
        }

        // 요청자가 약속 관계자인지 체크
        if(!Objects.equals(user.getId(), appointment.getOwner().getId()) && !Objects.equals(user.getId(), appointment.getRequester().getId())) {
            throw new CustomException(CustomExceptionCode.NOT_APPOINTMENT_PARTICIPANT, null);
        }

        // 제품 삭제 여부 확인
        if(item.getIsDeleted()) {
            throw new CustomException(CustomExceptionCode.ALREADY_DELETED_ITEM, null);
        }

        // 가격 협의가 불가능한데 가격을 바꿔서 요청을 보내는 경우, 예외 처리
        if(!item.getCanDeal() && (dto.getPrice() != appointment.getPrice() || dto.getDeposit() != appointment.getDeposit())) {
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
            checkRentalPeriodPossibility(dto.getRentalDate(), dto.getReturnDate(), item);
        }
        // 판매 게시글의 경우
        else
        {
            // 구매 날짜 가능 여부 체크
            checkSaleDatePossibility(dto.getRentalDate(), item);
        }
    }
}
