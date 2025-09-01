package com.airfryer.repicka.domain.appointment.service;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.firebase.dto.FCMNotificationReq;

import com.airfryer.repicka.domain.chat.repository.ChatRoomRepository;
import com.airfryer.repicka.domain.notification.entity.NotificationType;
import com.airfryer.repicka.common.redis.RedisService;
import com.airfryer.repicka.common.firebase.service.FCMService;
import com.airfryer.repicka.common.redis.dto.AppointmentTask;
import com.airfryer.repicka.common.redis.type.TaskType;
import com.airfryer.repicka.domain.appointment.dto.*;
import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.appointment.entity.AppointmentType;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
import com.airfryer.repicka.domain.chat.dto.EnterChatRoomRes;
import com.airfryer.repicka.domain.chat.entity.Chat;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.chat.entity.ParticipateChatRoom;
import com.airfryer.repicka.domain.chat.repository.ParticipateChatRoomRepository;
import com.airfryer.repicka.domain.chat.service.ChatService;
import com.airfryer.repicka.domain.chat.service.ChatWebSocketService;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item.repository.ItemRepository;
import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import com.airfryer.repicka.domain.item_image.repository.ItemImageRepository;
import com.airfryer.repicka.domain.item.entity.TransactionType;
import com.airfryer.repicka.domain.review.entity.Review;
import com.airfryer.repicka.domain.review.repository.ReviewRepository;
import com.airfryer.repicka.domain.user.entity.user.User;
import com.airfryer.repicka.domain.notification.NotificationService;

import com.airfryer.repicka.domain.user.repository.UserBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ParticipateChatRoomRepository participateChatRoomRepository;
    private final UserBlockRepository userBlockRepository;
    private final ReviewRepository reviewRepository;

    private final AppointmentUtil appointmentUtil;
    private final ChatService chatService;
    private final ChatWebSocketService chatWebSocketService;
    private final RedisService delayedQueueService;
    private final FCMService fcmService;
    private final NotificationService notificationService;

    /// 서비스

    // 약속 제시
    @Transactional
    public EnterChatRoomRes proposeAppointment(User requester, OfferAppointmentReq dto, boolean isRental)
    {
        /// 제품 데이터 조회

        // 제품 데이터 조회
        Item item = itemRepository.findById(dto.getItemId())
            .orElseThrow(() -> new CustomException(CustomExceptionCode.ITEM_NOT_FOUND, dto.getItemId()));

        /// 예외 처리

        // 제품 삭제 여부 확인
        if(item.getIsDeleted()) {
            throw new CustomException(CustomExceptionCode.ALREADY_DELETED_ITEM, null);
        }

        // 대여(구매)가 가능한 제품인지 확인
        if(!Arrays.asList(item.getTransactionTypes()).contains(isRental ? TransactionType.RENTAL : TransactionType.SALE)) {
            throw new CustomException(isRental ? CustomExceptionCode.CANNOT_RENTAL_ITEM :CustomExceptionCode.CANNOT_SALE_ITEM, null);
        }

        // 가격 협의가 불가능한데 가격을 바꾸지는 않았는지 체크
        if(
            !item.getCanDeal() &&
            (
                (dto.getPrice() != (isRental ? item.getRentalFee() : item.getSalePrice())) ||
                (isRental && (dto.getDeposit() != item.getDeposit()))
            )
        ) {
            throw new CustomException(CustomExceptionCode.DEAL_NOT_ALLOWED, null);
        }

        // 요청자와 제품 소유자가 다른 사용자인지 체크
        if(Objects.equals(requester.getId(), item.getOwner().getId())) {
            throw new CustomException(CustomExceptionCode.SAME_OWNER_AND_REQUESTER, null);
        }

        // 유저 차단 데이터 존재 여부 체크
        if(userBlockRepository.existsByUserIds(requester.getId(), item.getOwner().getId())) {
            throw new CustomException(CustomExceptionCode.USER_BLOCK_EXIST, null);
        }

        // 대여(구매) 날짜 가능 여부 체크
        if(isRental) {
            appointmentUtil.checkRentalPeriodPossibility(dto.getStartDate(), dto.getEndDate(), item);
        } else {
            appointmentUtil.checkSaleDatePossibility(dto.getStartDate(), item);
        }

        // 완료되지 않은 약속 데이터가 존재하지 않는지 체크
        List<Appointment> currentAppointmentOptional = appointmentRepository.findByItemIdAndOwnerIdAndRequesterIdAndStateIn(
            item.getId(),
            item.getOwner().getId(),
            requester.getId(),
            List.of(AppointmentState.PENDING, AppointmentState.CONFIRMED, AppointmentState.IN_PROGRESS)
        );

        if(!currentAppointmentOptional.isEmpty()) {
            throw new CustomException(CustomExceptionCode.CURRENT_APPOINTMENT_EXIST, null);
        }

        /// 채팅방 조회 (존재하지 않으면 생성)

        ChatRoom chatRoom = chatService.createChatRoom(item, requester);

        /// 채팅방 재입장

        // 요청자의 채팅방 참여 정보 조회
        ParticipateChatRoom requesterParticipateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoom.getId(), requester.getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

        // 제품 소유자의 채팅방 참여 정보 조회
        ParticipateChatRoom ownerParticipateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoom.getId(), item.getOwner().getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

        // 요청자가 이미 채팅방을 나간 경우
        if(requesterParticipateChatRoom.getHasLeftRoom())
        {
            // 채팅방 재입장 처리
            requesterParticipateChatRoom.reEnter();

            // 채팅방 재입장 채팅 생성
            Chat reEnterChat = Chat.builder()
                .chatRoomId(chatRoom.getId())
                .userId(requester.getId())
                .nickname(requester.getNickname())
                .content(requester.getNickname() + " 님께서 채팅방에 재입장하였습니다.")
                .isNotification(true)
                .isPick(false)
                .pickInfo(null)
                .build();

            // 채팅방 재입장 채팅 전송
            chatWebSocketService.sendMessageChat(requester, chatRoom, reEnterChat);
        }

        // 제품 소유자가 이미 채팅방을 나간 경우
        if(ownerParticipateChatRoom.getHasLeftRoom())
        {
            // 채팅방 재입장 처리
            ownerParticipateChatRoom.reEnter();

            // 채팅방 재입장 채팅 생성
            Chat reEnterChat = Chat.builder()
                .chatRoomId(chatRoom.getId())
                .userId(item.getOwner().getId())
                .nickname(item.getOwner().getNickname())
                .content(item.getOwner().getNickname() + " 님께서 채팅방에 재입장하였습니다.")
                .isNotification(true)
                .isPick(false)
                .pickInfo(null)
                .build();

            // 채팅방 재입장 채팅 전송
            chatWebSocketService.sendMessageChat(item.getOwner(), chatRoom, reEnterChat);
        }

        /// 새로운 약속 데이터 생성

        // 새로운 약속 데이터
        Appointment appointment = Appointment.of(item, requester, dto, isRental);

        // 약속 데이터 저장
        appointmentRepository.save(appointment);

        /// 제품 소유자에게 약속 제시 알림

        FCMNotificationReq notificationReq = FCMNotificationReq.of(NotificationType.APPOINTMENT_PROPOSAL, appointment.getId().toString(), requester.getNickname());
        fcmService.sendNotification(item.getOwner().getFcmToken(), notificationReq);

        // 약속 제시 알림 저장
        notificationService.saveNotification(item.getOwner(), NotificationType.APPOINTMENT_PROPOSAL, appointment);

        /// PICK 메시지 전송

        // 채팅 생성
        Chat chat = Chat.builder()
            .chatRoomId(chatRoom.getId())
            .userId(requester.getId())
            .nickname(requester.getNickname())
            .content(requester.getNickname() + " 님께서 설정하신 " + (isRental ? "대여" : "구매") + " 정보가 도착했어요.")
            .isNotification(false)
            .isPick(true)
            .pickInfo(Chat.PickInfo.from(appointment))
            .build();

        // 채팅 전송
        chatWebSocketService.sendMessageChat(requester, chatRoom, chat);

        /// 채팅방 입장 데이터 반환

        return chatService.enterChatRoom(requester, chatRoom, 1);
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
            appointmentUtil.checkRentalPeriodPossibility(
                    appointment.getRentalDate(),
                    appointment.getReturnDate(),
                    item
            );
        }
        // 구매 약속의 경우
        else
        {
            // 구매 날짜 가능 여부 체크
            appointmentUtil.checkSaleDatePossibility(appointment.getRentalDate(), item);

            // 제품의 판매 예정 날짜 변경
            item.confirmSale(LocalDateTime.now());
        }

        /// 약속 확정

        appointment.confirm();

        /// 약속 확정 채팅

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByItemIdAndOwnerIdAndRequesterId(item.getId(), appointment.getOwner().getId(), appointment.getRequester().getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.CHATROOM_NOT_FOUND, null));

        // 약속 확정 채팅 생성
        Chat chat = Chat.builder()
                .chatRoomId(chatRoom.getId())
                .userId(user.getId())
                .nickname(user.getNickname())
                .content(user.getNickname() + " 님께서 약속을 확정하였습니다.")
                .isNotification(true)
                .isPick(false)
                .pickInfo(null)
                .build();

        // 약속 확정 채팅 전송
        chatWebSocketService.sendMessageChat(user, chatRoom, chat);

        /// 약속 확정 알림

        // 약속 확정 알림 내역 저장
        notificationService.saveNotification(appointment.getOwner(), NotificationType.APPOINTMENT_CONFIRM, appointment);
        notificationService.saveNotification(appointment.getRequester(), NotificationType.APPOINTMENT_CONFIRM, appointment);

        // 약속 확정 푸시알림 전송
        FCMNotificationReq notificationReq = FCMNotificationReq.of(NotificationType.APPOINTMENT_CONFIRM, appointment.getId().toString(), appointment.getItem().getTitle());
        fcmService.sendNotification(appointment.getCreator().getFcmToken(), notificationReq);

        // 약속 푸시알림 전송 예약
        delayedQueueService.addDelayedTask(
                TaskType.RENTAL_REMIND.name(),
                AppointmentTask.from(appointment, TaskType.RENTAL_REMIND),
                appointment.getRentalDate().minusMinutes(60)
        );
        if(appointment.getType() == AppointmentType.RENTAL)
        {
            delayedQueueService.addDelayedTask(
                    TaskType.RETURN_REMIND.name(),
                    AppointmentTask.from(appointment, TaskType.RETURN_REMIND),
                    appointment.getReturnDate().minusMinutes(60)
            );
        }

        // 대여중 처리 예약
        delayedQueueService.addDelayedTask(
                TaskType.IN_PROGRESS.name(),
                AppointmentTask.from(appointment, TaskType.IN_PROGRESS),
                appointment.getRentalDate()
        );

        // 약속 데이터 반환
        return AppointmentRes.from(appointment);
    }

    // 약속 취소(거절)
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

        /// 약속 취소(거절) 푸시알림 전송 및 알림 내역 저장

        User opponent = Objects.equals(appointment.getRequester().getId(), user.getId()) ? appointment.getOwner() : appointment.getRequester();

        if(appointment.getState() == AppointmentState.PENDING)
        {
            // 상대방이 제시한 약속인 경우에는 거절 알림 처리
            if(Objects.equals(user.getId(), appointment.getCreator().getId()))
            {
                // 약속 취소 푸시알림 전송
                FCMNotificationReq notificationReq = FCMNotificationReq.of(NotificationType.APPOINTMENT_CANCEL, appointment.getId().toString(), user.getNickname());
                fcmService.sendNotification(opponent.getFcmToken(), notificationReq);

                // 약속 취소 알림 내역 저장
                notificationService.saveNotification(opponent, NotificationType.APPOINTMENT_CANCEL, appointment);
            }
            else
            {
                // 약속 거절 푸시알림 전송
                FCMNotificationReq notificationReq = FCMNotificationReq.of(NotificationType.APPOINTMENT_REJECT, appointment.getId().toString(), user.getNickname());
                fcmService.sendNotification(opponent.getFcmToken(), notificationReq);

                // 약속 거절 알림 내역 저장
                notificationService.saveNotification(opponent, NotificationType.APPOINTMENT_REJECT, appointment);
            }
        }
        else
        {
            // 약속 취소 푸시알림 전송
            FCMNotificationReq notificationReq = FCMNotificationReq.of(NotificationType.APPOINTMENT_CANCEL, appointment.getId().toString(), user.getNickname());
            fcmService.sendNotification(opponent.getFcmToken(), notificationReq);

            // 약속 취소 알림 내역 저장
            notificationService.saveNotification(appointment.getRequester(), NotificationType.APPOINTMENT_CANCEL, appointment);
            notificationService.saveNotification(appointment.getOwner(), NotificationType.APPOINTMENT_CANCEL, appointment);
        }

        /// 약속 취소

        appointmentUtil.cancelAppointment(appointment);

        /// 채팅방 조회

        ChatRoom chatRoom = chatRoomRepository.findByItemIdAndOwnerIdAndRequesterId(item.getId(), appointment.getOwner().getId(), appointment.getRequester().getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.CHATROOM_NOT_FOUND, null));

        /// 약속 취소 채팅 전송

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

        // TODO: 사용자 피드백 요청

        // 약속 데이터 반환
        return AppointmentRes.from(appointment);
    }

    // 약속 상세 조회
    @Transactional(readOnly = true)
    public AppointmentInfo getAppointmentDetail(User user, Long appointmentId)
    {
        /// 약속 조회

        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new CustomException(CustomExceptionCode.APPOINTMENT_NOT_FOUND, appointmentId));

        /// 예외 처리

        if(!appointment.getRequester().getId().equals(user.getId()) && !appointment.getOwner().getId().equals(user.getId())) {
            throw new CustomException(CustomExceptionCode.NOT_APPOINTMENT_PARTICIPANT, null);
        }

        /// 채팅방 조회

        ChatRoom chatRoom = chatRoomRepository.findByItemIdAndOwnerIdAndRequesterId(appointment.getItem().getId(), appointment.getOwner().getId(), appointment.getRequester().getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.CHATROOM_NOT_FOUND, null));

        /// 대표 이미지 조회

        ItemImage thumbnail = itemImageRepository.findFirstByItemId(appointment.getItem().getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.ITEM_IMAGE_NOT_FOUND, null));

        /// 리뷰 조회

        User opponent = Objects.equals(chatRoom.getRequester().getId(), user.getId()) ? chatRoom.getOwner() : chatRoom.getRequester();
        Optional<Review> reviewOptional = reviewRepository.findByReviewedIdAndReviewerIdAndAppointmentId(opponent.getId(), user.getId(), appointment.getId());

        /// 데이터 반환

        return AppointmentInfo.from(user, appointment, chatRoom, thumbnail.getFileKey(), reviewOptional.isPresent());
    }

    // 나의 약속 페이지 조회 (나의 PICK 조회)
    // (확정/대여중/완료) 상태인 나의 약속 페이지 조회
    @Transactional(readOnly = true)
    public AppointmentPageRes findMyAppointmentPage(User user, FindMyAppointmentPageReq dto)
    {
        /// 약속 페이지 조회

        List<Appointment> appointmentPage = dto.getSubject().findAppointmentPage(
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

        /// (제품 ID, 썸네일 URL) 맵 생성

        // 썸네일 리스트 조회
        List<ItemImage> thumbnailList = itemImageRepository.findThumbnailListByItemIdList(appointmentPage.stream().map(appointment -> appointment.getItem().getId()).toList());

        //(제품 ID, 썸네일 URL) 맵 생성
        Map<Long, String> itemIdThumbnailUrlMap = thumbnailList.stream()
                .collect(Collectors.toMap(
                        itemImage -> itemImage.getItem().getId(),
                        ItemImage::getFileKey
                ));

        /// 약속 정보 리스트 생성

        List<AppointmentInfo> appointmentInfoList = appointmentPage.stream().map(appointment -> {

            // 채팅방 조회
            ChatRoom chatRoom = chatRoomRepository.findByItemIdAndOwnerIdAndRequesterId(appointment.getItem().getId(), appointment.getOwner().getId(), appointment.getRequester().getId())
                    .orElseThrow(() -> new CustomException(CustomExceptionCode.CHATROOM_NOT_FOUND, null));

            // 리뷰 조회
            User opponent = Objects.equals(chatRoom.getRequester().getId(), user.getId()) ? chatRoom.getOwner() : chatRoom.getRequester();
            Optional<Review> reviewOptional = reviewRepository.findByReviewedIdAndReviewerIdAndAppointmentId(opponent.getId(), user.getId(), appointment.getId());


            return AppointmentInfo.from(user, appointment, chatRoom, itemIdThumbnailUrlMap.get(appointment.getItem().getId()), reviewOptional.isPresent());

        }).toList();

        /// 데이터 반환

        return AppointmentPageRes.of(
                appointmentInfoList,
                cursorState,
                cursorDate,
                cursorId,
                hasNext
        );
    }

    // 협의 중이거나 확정된 약속 수정
    @Transactional
    public AppointmentRes updateAppointment(User user, UpdateAppointmentReq dto)
    {
        /// 약속 데이터 조회

        // 약속 조회
        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
            .orElseThrow(() -> new CustomException(CustomExceptionCode.APPOINTMENT_NOT_FOUND, dto.getAppointmentId()));

        // 약속 확정 여부
        boolean isConfirmed = appointment.getState() == AppointmentState.CONFIRMED;

        /// 약속 수정

        appointment.update(user, dto, appointment.getType() == AppointmentType.RENTAL);

        /// 약속 수정 가능 여부 체크

        appointmentUtil.checkUpdateAppointmentPossibility(
                appointment,
                user,
                dto
        );

        /// 채팅방 조회

        ChatRoom chatRoom = chatRoomRepository.findByItemIdAndOwnerIdAndRequesterId(appointment.getItem().getId(), appointment.getOwner().getId(), appointment.getRequester().getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.CHATROOM_NOT_FOUND, null));

        /// 확정된 약속의 경우, 약속 취소 채팅 및 알림 전송

        if(isConfirmed)
        {
            /// 약속 취소 채팅 전송

            Chat cancelChat = Chat.builder()
                    .chatRoomId(chatRoom.getId())
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .content(user.getNickname() + " 님께서 약속을 취소하였습니다.")
                    .isNotification(true)
                    .isPick(false)
                    .pickInfo(null)
                    .build();

            chatWebSocketService.sendMessageChat(user, chatRoom, cancelChat);

            /// 채팅 상대방에게 약속 취소 알림 전송

            // 채팅 상대방 조회
            User opponent = Objects.equals(chatRoom.getRequester().getId(), user.getId()) ? chatRoom.getOwner() : chatRoom.getRequester();

            // 푸시 알림 전송
            FCMNotificationReq notificationReq = FCMNotificationReq.of(NotificationType.APPOINTMENT_CANCEL, appointment.getId().toString(), user.getNickname());
            fcmService.sendNotification(opponent.getFcmToken(), notificationReq);

            /// 약속 취소 알림 저장

            notificationService.saveNotification(appointment.getRequester(), NotificationType.APPOINTMENT_CANCEL, appointment);
            notificationService.saveNotification(appointment.getOwner(), NotificationType.APPOINTMENT_CANCEL, appointment);
        }

        /// PICK 메시지 전송

        // PICK 메시지 생성
        Chat chat = Chat.builder()
            .chatRoomId(chatRoom.getId())
            .userId(user.getId())
            .nickname(user.getNickname())
            .content(user.getNickname() + " 님께서 설정하신 " + (appointment.getType() == AppointmentType.RENTAL ? "대여" : "구매") + " 정보가 도착했어요.")
            .isNotification(false)
            .isPick(true)
            .pickInfo(Chat.PickInfo.from(appointment))
            .build();

        // PICK 메시지 전송
        chatWebSocketService.sendMessageChat(user, chatRoom, chat);

        /// 데이터 반환

        // 약속 데이터 반환
        return AppointmentRes.from(appointment);
    }

    // 대여중인 약속 존재 여부 확인
    @Transactional(readOnly = true)
    public boolean isInProgressAppointmentPresent(User user, Long chatRoomId)
    {
        /// 채팅방 참여 데이터 조회

        // 채팅방 참여 데이터 조회
        ParticipateChatRoom participateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoomId, user.getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

        // 이미 채팅방을 나갔는지 확인
        if(participateChatRoom.getHasLeftRoom()) {
            throw new CustomException(CustomExceptionCode.ALREADY_LEFT_CHATROOM, null);
        }

        /// 채팅방 조회

        ChatRoom chatRoom = participateChatRoom.getChatRoom();

        /// 대여중인 약속 조회

        List<Appointment> inProgressAppointmentOptional = appointmentRepository.findByItemIdAndOwnerIdAndRequesterIdAndStateIn(
                chatRoom.getItem().getId(),
                chatRoom.getOwner().getId(),
                chatRoom.getRequester().getId(),
                List.of(AppointmentState.IN_PROGRESS)
        );

        return !inProgressAppointmentOptional.isEmpty();
    }
}
