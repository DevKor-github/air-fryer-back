package com.airfryer.repicka.domain.appointment.service;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.domain.appointment.FindMyAppointmentPeriod;
import com.airfryer.repicka.domain.appointment.FindMyAppointmentSubject;
import com.airfryer.repicka.domain.appointment.dto.*;
import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.appointment.entity.UpdateInProgressAppointment;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
import com.airfryer.repicka.domain.appointment.repository.UpdateInProgressAppointmentRepository;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item.repository.ItemRepository;
import com.airfryer.repicka.domain.item_image.ItemImageService;
import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import com.airfryer.repicka.domain.item_image.repository.ItemImageRepository;
import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.post.entity.PostType;
import com.airfryer.repicka.domain.post.repository.PostRepository;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService
{
    private final AppointmentRepository appointmentRepository;
    private final UpdateInProgressAppointmentRepository updateInProgressAppointmentRepository;
    private final PostRepository postRepository;
    private final ItemImageRepository itemImageRepository;

    private final ItemImageService itemImageService;

    /// 서비스

    // 대여 게시글에서 약속 제시
    @Transactional
    public void offerAppointmentInRentalPost(User borrower, OfferAppointmentInRentalPostReq dto)
    {
        /// 게시글 데이터 조회

        // 게시글 데이터 조회
        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.POST_NOT_FOUND, dto.getPostId()));

        // 대여 게시글인지 체크
        if(post.getPostType() != PostType.RENTAL) {
            throw new CustomException(CustomExceptionCode.NOT_RENTAL_POST, post.getPostType());
        }

        // 게시글 작성자와 대여자가 다른 사용자인지 체크
        if(Objects.equals(post.getWriter(), borrower)) {
            throw new CustomException(CustomExceptionCode.SAME_WRITER_AND_REQUESTER, null);
        }

        /// 제품 데이터 조회

        // 제품 데이터 조회
        Item item = post.getItem();

        // 가격 협의가 불가능한데 가격을 바꾸지는 않았는지 체크
        if(!item.getCanDeal() && (dto.getPrice() != post.getPrice() || dto.getDeposit() != post.getDeposit())) {
            throw new CustomException(CustomExceptionCode.DEAL_NOT_ALLOWED, null);
        }

        // 대여 구간 가능 여부 체크
        checkRentalPeriodPossibility(dto.getRentalDate(), dto.getReturnDate(), post, item);

        /// 게시글 작성자와 대여자 간의 협의 중인 약속 데이터가 이미 존재한다면, 기존 약속 데이터를 수정
        /// 게시글 작성자와 대여자 간의 협의 중인 약속 데이터가 존재하지 않는다면, 새로운 약속 데이터를 생성

        // 게시글 작성자와 대여자 간의 협의 중인 약속 데이터 조회
        List<Appointment> pendingAppointmentOptional = appointmentRepository.findByPostIdAndOwnerIdAndRequesterIdAndState(
                post.getId(),
                post.getWriter().getId(),
                borrower.getId(),
                AppointmentState.PENDING
        );

        // 협의 중인 약속 데이터가 존재하는 경우, 기존 약속 데이터를 수정
        if(!pendingAppointmentOptional.isEmpty())
        {
            // 기존에 존재하던 약속 데이터
            Appointment pendingAppointment = pendingAppointmentOptional.getFirst();

            // 약속 데이터 수정
            pendingAppointment.updateAppointment(dto);

            // 약속 데이터 저장
            appointmentRepository.save(pendingAppointment);
        }
        // 협의 중인 약속 데이터가 존재하지 않는 경우, 새로운 약속 데이터를 생성
        else
        {
            // 새로운 약속 데이터 생성
            Appointment appointment = Appointment.builder()
                    .post(post)
                    .creator(borrower)
                    .owner(post.getWriter())
                    .requester(borrower)
                    .rentalLocation(dto.getRentalLocation().trim())
                    .returnLocation(dto.getReturnLocation().trim())
                    .rentalDate(dto.getRentalDate())
                    .returnDate(dto.getReturnDate())
                    .price(dto.getPrice())
                    .deposit(dto.getDeposit())
                    .state(AppointmentState.PENDING)
                    .build();

            // 약속 데이터 저장
            appointmentRepository.save(appointment);
        }

        // TODO: 채팅방 데이터와 약속 데이터를 반환해야 함.
    }

    // 판매 게시글에서 약속 제시
    @Transactional
    public void offerAppointmentInSalePost(User buyer, OfferAppointmentInSalePostReq dto)
    {
        /// 게시글 데이터 조회

        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.POST_NOT_FOUND, dto.getPostId()));

        // 판매 게시글인지 체크
        if(post.getPostType() != PostType.SALE) {
            throw new CustomException(CustomExceptionCode.NOT_SALE_POST, post.getPostType());
        }

        // 게시글 작성자와 대여자가 다른 사용자인지 체크
        if(Objects.equals(post.getWriter(), buyer)) {
            throw new CustomException(CustomExceptionCode.SAME_WRITER_AND_REQUESTER, null);
        }

        /// 제품 데이터 조회

        // 제품 데이터 조회
        Item item = post.getItem();

        // 가격 협의가 불가능한데 가격을 바꾸지는 않았는지 체크
        if(!item.getCanDeal() && (dto.getPrice() != post.getPrice())) {
            throw new CustomException(CustomExceptionCode.DEAL_NOT_ALLOWED, null);
        }

        // 구매 날짜 가능 여부 체크
        checkSaleDatePossibility(dto.getSaleDate(), item);

        /// 게시글 작성자와 구매자 간의 협의 중인 약속 데이터가 이미 존재한다면, 기존 약속 데이터를 수정
        /// 게시글 작성자와 구매자 간의 협의 중인 약속 데이터가 존재하지 않는다면, 새로운 약속 데이터를 생성

        // 게시글 작성자와 구매자 간의 협의 중인 약속 데이터 조회
        List<Appointment> pendingAppointmentOptional = appointmentRepository.findByPostIdAndOwnerIdAndRequesterIdAndState(
                post.getId(),
                post.getWriter().getId(),
                buyer.getId(),
                AppointmentState.PENDING
        );

        // 협의 중인 약속 데이터가 존재하는 경우, 기존 약속 데이터를 수정
        if(!pendingAppointmentOptional.isEmpty())
        {
            // 기존에 존재하던 약속 데이터
            Appointment pendingAppointment = pendingAppointmentOptional.getFirst();

            // 약속 데이터 수정
            pendingAppointment.updateAppointment(dto);

            // 약속 데이터 저장
            appointmentRepository.save(pendingAppointment);
        }
        // 협의 중인 약속 데이터가 존재하지 않는 경우, 새로운 약속 데이터를 생성
        else
        {
            // 새로운 약속 데이터 생성
            Appointment appointment = Appointment.builder()
                    .post(post)
                    .creator(buyer)
                    .owner(post.getWriter())
                    .requester(buyer)
                    .rentalLocation(dto.getSaleLocation().trim())
                    .returnLocation(null)
                    .rentalDate(dto.getSaleDate())
                    .returnDate(null)
                    .price(dto.getPrice())
                    .deposit(0)
                    .state(AppointmentState.PENDING)
                    .build();

            // 약속 데이터 저장
            appointmentRepository.save(appointment);
        }

        // TODO: 채팅방 데이터와 약속 데이터를 반환해야 함.
    }

    // 약속 확정
    @Transactional
    public AppointmentRes confirmAppointment(User user, Long appointmentId)
    {
        /// 약속 데이터 조회

        // 약속 데이터 조회
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.APPOINTMENT_NOT_FOUND, appointmentId));

        // 제시 중인 약속인지 체크
        if(appointment.getState() != AppointmentState.PENDING) {
            throw new CustomException(CustomExceptionCode.NOT_PENDING_APPOINTMENT, null);
        }

        // 동의하는 사용자가 약속을 생성한 사용자와 다른지 체크
        if(Objects.equals(user.getId(), appointment.getCreator().getId())) {
            throw new CustomException(CustomExceptionCode.CANNOT_CONFIRM_APPOINTMENT_MYSELF, null);
        }

        // 동의자가 약속 관계자인지 체크
        if(!Objects.equals(user.getId(), appointment.getOwner().getId()) && !Objects.equals(user.getId(), appointment.getRequester().getId())) {
            throw new CustomException(CustomExceptionCode.NOT_APPOINTMENT_PARTICIPANT, null);
        }

        /// 게시글 데이터 조회

        // 게시글 데이터 조회
        Post post = appointment.getPost();

        /// 제품 데이터 조회

        // 제품 데이터 조회
        Item item = post.getItem();

        // 대여 약속의 경우
        if(post.getPostType() == PostType.RENTAL)
        {
            // 대여 구간 가능 여부 체크
            checkRentalPeriodPossibility(
                    appointment.getRentalDate(),
                    appointment.getReturnDate(),
                    post,
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

        /// 약속 상태 변경

        appointment.confirmAppointment();

        /// 약속 데이터 반환

        return AppointmentRes.from(appointment, post);
    }

    // 약속 취소
    @Transactional
    public AppointmentRes cancelAppointment(User user, Long appointmentId)
    {
        /// 약속 데이터 조회

        // 약속 데이터 조회
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.APPOINTMENT_NOT_FOUND, appointmentId));

        // 제시 중이거나 확정된 약속인지 체크
        if(appointment.getState() != AppointmentState.PENDING && appointment.getState() != AppointmentState.CONFIRMED) {
            throw new CustomException(CustomExceptionCode.APPOINTMENT_CANNOT_CANCELLED, appointment.getState());
        }

        // 취소자가 약속 관계자인지 체크
        if(!Objects.equals(user.getId(), appointment.getOwner().getId()) && !Objects.equals(user.getId(), appointment.getRequester().getId())) {
            throw new CustomException(CustomExceptionCode.NOT_APPOINTMENT_PARTICIPANT, null);
        }

        /// 게시글 데이터 조회

        // 게시글 데이터 조회
        Post post = appointment.getPost();

        /// 제품 데이터 조회

        // 제품 데이터 조회
        Item item = post.getItem();

        // 구매 약속의 경우
        if(post.getPostType() == PostType.SALE)
        {
            // 제품의 판매 예정 날짜 변경
            item.cancelSale();
        }

        /// 약속 상태 변경

        appointment.cancelAppointment();

        // TODO: 사용자 피드백 요청
        // TODO: 채팅방 제거

        /// 약속 데이터 반환

        return AppointmentRes.from(appointment, post);
    }

    // (확정/대여중/완료) 상태의 나의 약속 페이지 조회
    @Transactional(readOnly = true)
    public AppointmentPageRes findMyAppointmentPage(User user,
                                                    FindMyAppointmentSubject subject,
                                                    FindMyAppointmentPageReq dto)
    {
        /// 검색 시작 날짜

        LocalDateTime searchStartDate = period.calculateFromDate(LocalDateTime.now());

        /// 약속 페이지 조회

        Page<Appointment> appointmentPage = subject.findAppointmentPage(
                appointmentRepository,
                pageable,
                user,
                type,
                searchStartDate
        );

        /// 약속 리스트 생성 및 정렬

        // 약속 페이지 > 약속 리스트 변환
        List<Appointment> appointmentList = new ArrayList<>(appointmentPage.getContent());

        // 약속 리스트 정렬
        // AppointmentState 기준 : CONFIRMED > IN_PROGRESS > SUCCESS
        // 동일한 AppointmentState 내에서는 rentalDate 내림차순
        appointmentList.sort(
                Comparator
                        .comparing((Appointment a) -> {
                            return switch (a.getState()) {
                                case CONFIRMED -> 1;
                                case IN_PROGRESS -> 2;
                                case SUCCESS -> 3;
                                default -> 4;
                            };
                        })
                        .thenComparing(Appointment::getRentalDate, Comparator.reverseOrder())
        );

        /// 대표 이미지 리스트 조회

        List<ItemImage> thumbnailList = itemImageRepository.findThumbnailListByItemIdList(appointmentList.stream().map(appointment -> {
            return appointment.getPost().getItem().getId();
        }).toList());

        // Map(제품 ID, 대표 이미지 URL) 생성
        Map<Long, String> thumbnailUrlMap = thumbnailList.stream()
                .collect(Collectors.toMap(
                        itemImage -> itemImage.getItem().getId(),
                        itemImage -> itemImageService.getFullImageUrl(itemImage)
                ));

        // Map(약속, 대표 이미지 URL) 생성
        Map<Appointment, Optional<String>> map = appointmentList.stream()
                .collect(Collectors.toMap(
                        appointment -> appointment,
                        appointment -> Optional.ofNullable(thumbnailUrlMap.get(appointment.getPost().getItem().getId()))
                ));

        /// 데이터 반환

        return AppointmentPageRes.of(map, type, pageable.getPageNumber(), appointmentPage.getTotalPages());
    }

    // 확정된 약속 변경 제시
    @Transactional
    public AppointmentRes offerToUpdateConfirmedAppointment(User user, OfferToUpdateConfirmedAppointmentReq dto)
    {
        /// 약속 데이터 조회

        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.APPOINTMENT_NOT_FOUND, dto.getAppointmentId()));

        // 확정된 약속인지 체크
        if(appointment.getState() != AppointmentState.CONFIRMED) {
            throw new CustomException(CustomExceptionCode.NOT_CONFIRMED_APPOINTMENT, appointment.getState());
        }

        // 요청자가 약속 관계자인지 체크
        if(!Objects.equals(user.getId(), appointment.getOwner().getId()) && !Objects.equals(user.getId(), appointment.getRequester().getId())) {
            throw new CustomException(CustomExceptionCode.NOT_APPOINTMENT_PARTICIPANT, null);
        }

        /// 게시글 데이터 조회

        Post post = appointment.getPost();

        /// 제품 데이터 조회

        Item item = post.getItem();

        // 가격 협의가 불가능한데 가격을 바꿔서 요청을 보내는 경우, 예외 처리
        if(!item.getCanDeal() && (dto.getPrice() != post.getPrice() || dto.getDeposit() != post.getDeposit())) {
            throw new CustomException(CustomExceptionCode.DEAL_NOT_ALLOWED, null);
        }

        // 약속 상태 변경
        appointment.cancelAppointment();

        // 대여 게시글의 경우
        if(post.getPostType() == PostType.RENTAL)
        {
            // 대여 구간 가능 여부 체크
            checkRentalPeriodPossibility(dto.getRentalDate(), dto.getReturnDate(), post, item);
        }
        // 판매 게시글의 경우
        else
        {
            // 제품의 판매 예정 날짜 변경
            item.cancelSale();

            // 구매 날짜 가능 여부 체크
            checkSaleDatePossibility(dto.getRentalDate(), item);
        }

        /// 새로운 약속 데이터 생성

        // 새로운 약속 데이터 생성
        Appointment newAppointment = appointment.clone();
        newAppointment.updateAppointment(user, dto, post.getPostType() == PostType.RENTAL);

        // 약속 데이터 저장
        appointmentRepository.save(newAppointment);

        /// 약속 데이터 반환

        return AppointmentRes.from(newAppointment, post);
    }

    /// ============================ 공통 로직 ============================

    /// 해당 날짜에 예정된 대여 약속이 하나도 없는지 판별

    public boolean isPostAvailableOnDate(Long postId, LocalDateTime date) {
        return appointmentRepository.findListOverlappingWithPeriod(
                postId,
                AppointmentState.CONFIRMED,
                date,
                date
        ).isEmpty();
    }

    /// 해당 구간 동안 예정된 대여 약속이 하나도 존재하지 않는지 판별

    public boolean isPostAvailableOnInterval(Long postId, LocalDateTime startDate, LocalDateTime endDate)
    {
        if(endDate.isBefore(startDate)) {
            return true;
        }

        return appointmentRepository.findListOverlappingWithPeriod(
                postId,
                AppointmentState.CONFIRMED,
                startDate,
                endDate
        ).isEmpty();
    }

    public boolean isPostAvailableOnInterval(Long postId, LocalDateTime startDate)
    {
        return appointmentRepository.findListOverlappingWithPeriod(
                postId,
                AppointmentState.CONFIRMED,
                startDate
        ).isEmpty();
    }

    /// 제품 구매가 가능한 첫 날짜 조회

    public LocalDate getFirstSaleAvailableDate(Long itemId)
    {
        // 반환할 날짜
        LocalDate firstSaleAvailableDate = LocalDate.now();

        // 대여 게시글 데이터 조회
        Optional<Post> rentalPostOptional = postRepository.findByItemIdAndPostType(itemId, PostType.RENTAL);

        // 대여 게시글이 존재하는 경우
        if(rentalPostOptional.isPresent())
        {
            Post rentalPost = rentalPostOptional.get();

            // 예정된 대여 약속 중, 반납 날짜가 가장 늦은 약속 데이터 조회
            Optional<Appointment> appointmentOptional = appointmentRepository.findTop1ByPostIdAndStateOrderByReturnDateDesc(
                    rentalPost.getId(),
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
                                             Post post,
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
        if(!isPostAvailableOnInterval(post.getId(), startDate, endDate)) {
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
}
