package com.airfryer.repicka.domain.appointment.service;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.domain.appointment.FindMyAppointmentPeriod;
import com.airfryer.repicka.domain.appointment.FindMyAppointmentSubject;
import com.airfryer.repicka.domain.appointment.dto.*;
import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
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
    private final PostRepository postRepository;
    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;
    private final ItemImageService itemImageService;

    /// 서비스

    // 대여 게시글에서 약속 제시
    @Transactional
    public void offerAppointmentInRentalPost(User borrower, OfferAppointmentInRentalPostReq dto)
    {
        /// 예외 처리
        /// 1. 반납 일시가 대여 일시 이후인가?

        // 반납 일시가 대여 일시의 이후가 아닌 경우, 예외 처리
        if(!dto.getReturnDate().isAfter(dto.getRentalDate())) {
            throw new CustomException(CustomExceptionCode.RENTAL_DATE_IS_LATER_THAN_RETURN_DATE, Map.of(
                    "rentalDate", dto.getRentalDate(),
                    "returnDate", dto.getReturnDate()
            ));
        }

        /// 게시글 데이터 조회

        // 게시글 데이터 조회
        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.POST_NOT_FOUND, dto.getPostId()));

        /// 예외 처리
        /// 1. 대여 게시글인가?
        /// 2. 게시글 작성자(소유자)와 대여자가 다른 사용자인가?

        // 대여 게시글이 아닌 경우, 예외 처리
        if(post.getPostType() != PostType.RENTAL) {
            throw new CustomException(CustomExceptionCode.NOT_RENTAL_POST, post.getPostType());
        }

        // 게시글 작성자와 대여자가 동일한 경우, 예외 처리
        if(Objects.equals(post.getWriter(), borrower)) {
            throw new CustomException(CustomExceptionCode.SAME_WRITER_AND_REQUESTER, null);
        }

        /// 제품 데이터 조회

        // 제품 데이터 조회
        Item item = post.getItem();

        /// 예외 처리
        /// 1. 가격 협의가 불가능한데, 가격을 바꿔서 요청하지는 않았는가?
        /// 2. 대여를 원하는 구간 동안, 예정된 대여 약속이 하나도 존재하지 않는가?
        /// 3. 대여를 원하는 구간이 제품 판매 날짜 이전인가?

        // 가격 협의가 불가능한데 가격을 바꿔서 요청을 보내는 경우, 예외 처리
        if(!item.getCanDeal() && (dto.getPrice() != post.getPrice() || dto.getDeposit() != post.getDeposit())) {
            throw new CustomException(CustomExceptionCode.DEAL_NOT_ALLOWED, null);
        }

        // 대여를 원하는 구간 동안 예정된 대여 약속이 하나라도 존재하는 경우, 예외 처리
        if(!isPostAvailableOnInterval(post.getId(), dto.getRentalDate(), dto.getReturnDate())) {
            throw new CustomException(CustomExceptionCode.ALREADY_RENTAL_RESERVED_PERIOD, Map.of(
                    "rentalDate", dto.getRentalDate(),
                    "returnDate", dto.getReturnDate()
            ));
        }

        // 제품이 판매 예정 혹은 판매된 경우
        if(item.getSaleDate() != null)
        {
            // 대여를 원하는 구간이 판매 날짜 이전이 아닌 경우, 예외 처리
            if(!dto.getReturnDate().isBefore(item.getSaleDate())) {
                throw new CustomException(CustomExceptionCode.ALREADY_SALE_RESERVED_PERIOD, Map.of(
                        "rentalDate", dto.getRentalDate(),
                        "returnDate", dto.getReturnDate()
                ));
            }
        }

        /// 게시글 작성자와 대여자 간의 협의 중인 약속 데이터가 이미 존재한다면, 기존 약속 데이터를 수정
        /// 게시글 작성자와 대여자 간의 협의 중인 약속 데이터가 존재하지 않는다면, 새로운 약속 데이터를 생성

        // 게시글 작성자와 대여자 간의 협의 중인 약속 데이터 조회
        Optional<Appointment> pendingAppointmentOptional = appointmentRepository.findByPostIdAndOwnerAndRequesterAndState(
                post.getId(),
                post.getWriter(),
                borrower,
                AppointmentState.PENDING
        );

        // 협의 중인 약속 데이터가 존재하는 경우, 기존 약속 데이터를 수정
        if(pendingAppointmentOptional.isPresent())
        {
            // 기존에 존재하던 약속 데이터
            Appointment pendingAppointment = pendingAppointmentOptional.get();

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

        // 게시글 데이터 조회
        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.POST_NOT_FOUND, dto.getPostId()));

        /// 예외 처리
        /// 1. 판매 게시글인가?
        /// 2. 게시글 작성자(소유자)와 구매자가 다른 사용자인가?

        // 판매 게시글이 아닌 경우, 예외 처리
        if(post.getPostType() != PostType.SALE) {
            throw new CustomException(CustomExceptionCode.NOT_SALE_POST, post.getPostType());
        }

        // 게시글 작성자와 대여자가 동일한 경우, 예외 처리
        if(Objects.equals(post.getWriter(), buyer)) {
            throw new CustomException(CustomExceptionCode.SAME_WRITER_AND_REQUESTER, null);
        }

        /// 제품 데이터 조회

        // 제품 데이터 조회
        Item item = post.getItem();

        /// 예외 처리
        /// 1. 가격 협의가 불가능한데, 가격을 바꿔서 요청하지는 않았는가?
        /// 2. 제품이 이미 판매 예정이지 않는가?
        /// 3. 구매를 원하는 날짜 이후에 대여 약속이 존재하지 않는가?

        // 가격 협의가 불가능한데 가격을 바꿔서 요청을 보내는 경우, 예외 처리
        if(!item.getCanDeal() && (dto.getPrice() != post.getPrice())) {
            throw new CustomException(CustomExceptionCode.DEAL_NOT_ALLOWED, null);
        }

        // 판매 예정 혹은 판매된 제품인 경우, 예외 처리
        if(item.getSaleDate() != null) {
            throw new CustomException(CustomExceptionCode.ALREADY_SALE_RESERVED, item.getId());
        }

        // 제품 구매가 가능한 첫 날짜
        LocalDate firstSaleAvailableDate = getFirstSaleAvailableDate(item.getId());

        // 구매를 원하는 날짜가 구매가 가능한 첫 날짜 이전인 경우, 예외 처리
        if(firstSaleAvailableDate.isAfter(dto.getSaleDate().toLocalDate())) {
            throw new CustomException(CustomExceptionCode.ALREADY_RENTAL_RESERVED_PERIOD, Map.of(
                    "requestSaleDate", dto.getSaleDate(),
                    "firstAvailableSaleDate", firstSaleAvailableDate
            ));
        }

        /// 게시글 작성자와 구매자 간의 협의 중인 약속 데이터가 이미 존재한다면, 기존 약속 데이터를 수정
        /// 게시글 작성자와 구매자 간의 협의 중인 약속 데이터가 존재하지 않는다면, 새로운 약속 데이터를 생성

        // 게시글 작성자와 구매자 간의 협의 중인 약속 데이터 조회
        Optional<Appointment> pendingAppointmentOptional = appointmentRepository.findByPostIdAndOwnerAndRequesterAndState(
                post.getId(),
                post.getWriter(),
                buyer,
                AppointmentState.PENDING
        );

        // 협의 중인 약속 데이터가 존재하는 경우, 기존 약속 데이터를 수정
        if(pendingAppointmentOptional.isPresent())
        {
            // 기존에 존재하던 약속 데이터
            Appointment pendingAppointment = pendingAppointmentOptional.get();

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

        // TODO: 채팅방 데이터 반환해야 함.
    }

    // 월 단위로 날짜별 제품 대여 가능 여부 조회
    @Transactional(readOnly = true)
    public GetItemAvailabilityRes getItemRentalAvailability(Long rentalPostId, int year, int month)
    {
        // 해당 월의 길이
        int lastDayOfMonth = YearMonth.of(year, month).lengthOfMonth();

        /// 대여 게시글 데이터 조회

        // 대여 게시글 데이터 조회
        Post rentalPost = postRepository.findById(rentalPostId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.POST_NOT_FOUND, rentalPostId));

        /// 예외 처리
        /// 1. 대여 게시글인가?

        // 대여 게시글이 아닌 경우, 예외 처리
        if (rentalPost.getPostType() != PostType.RENTAL) {
            throw new CustomException(CustomExceptionCode.NOT_RENTAL_POST, rentalPost.getPostType());
        }

        /// 제품 데이터 조회

        // 제품 데이터 조회
        Item item = rentalPost.getItem();

        /// 반환할 날짜별 제품 대여 가능 여부 해시맵 생성 및 초기화

        // 반환할 날짜별 제품 대여 가능 여부 해시맵
        Map<LocalDate, Boolean> map = new LinkedHashMap<>();

        // 일단, 모든 날짜에 대여가 가능한 것으로 초기화
        for (int i = 1; i <= lastDayOfMonth; i++) {
            map.put(LocalDate.of(year, month, i), true);
        }

        /// 불가능 처리
        /// 1. 현재 이전의 날짜들은 전부 불가능 처리
        /// 2. 제품 판매 날짜부터는 전부 대여 불가능 처리
        /// 3. 해당 월 동안 예정된 모든 대여 약속들에 대해, 각 구간마다 대여 불가능 처리

        // 현재 이전의 날짜들은 전부 불가능 처리
        for(int i = 1; i <= lastDayOfMonth && LocalDate.of(year, month, i).isBefore(LocalDate.now()); i++) {
            map.put(LocalDate.of(year, month, i), false);
        }

        // 제품이 판매 예정 혹은 판매된 경우, 이후의 날짜들은 전부 대여 불가능 처리
        if(item.getSaleDate() != null)
        {
            LocalDate saleDate = item.getSaleDate().toLocalDate();  // 제품 판매 예정 날짜

            // 해당 월 이전에 이미 판매된 경우
            if(saleDate.isBefore(LocalDate.of(year, month, 1))) {
                for(int i = 1; i <= lastDayOfMonth; i++) {
                    map.put(LocalDate.of(year, month, i), false);
                }
            }
            // 해당 월 동안 판매되는 경우
            else if(
                    !saleDate.isBefore(LocalDate.of(year, month, 1)) &&
                    !saleDate.isAfter(LocalDate.of(year, month, lastDayOfMonth))
            ) {
                for(int i = saleDate.getDayOfMonth(); i <= lastDayOfMonth; i++) {
                    map.put(LocalDate.of(year, month, i), false);
                }
            }
        }

        // 해당 월 동안 존재하는 모든 대여 약속 조회
        List<Appointment> appointmentList = appointmentRepository.findListOverlappingWithPeriod(
                rentalPostId,
                AppointmentState.CONFIRMED,
                LocalDateTime.of(year, month, 1, 0, 0, 0),
                LocalDateTime.of(year, month, YearMonth.of(year, month).lengthOfMonth(), 23, 59, 59, 0)
        );

        // 모든 대여 약속 구간에 대해, 대여 불가능 처리
        for (Appointment appointment : appointmentList)
        {
            // 대여 시작 날짜가 해당 월 이전이고, 대여 종료 날짜가 해당 월 이후인 경우
            if(appointment.getRentalDate().getMonthValue() < month && appointment.getReturnDate().getMonthValue() > month)
            {
                map.replaceAll((key, value) -> false);
                break;
            }
            // 대여 시작 날짜가 해당 월에 속하고, 대여 종료 날짜가 해당 월 이후인 경우
            else if(appointment.getRentalDate().getMonthValue() == month && appointment.getReturnDate().getMonthValue() > month)
            {
                for(int i = appointment.getRentalDate().getDayOfMonth(); i <= lastDayOfMonth; i++)
                {
                    map.put(LocalDate.of(year, month, i), false);
                }
            }
            // 대여 시작 날짜가 해당 월 이전이고, 대여 종료 날짜가 해당 월에 속하는 경우
            else if(appointment.getRentalDate().getMonthValue() < month && appointment.getReturnDate().getMonthValue() == month)
            {
                for(int i = 1; i <= appointment.getReturnDate().getDayOfMonth(); i++)
                {
                    map.put(LocalDate.of(year, month, i), false);
                }
            }
            // 대여 시작 날짜 및 대여 종료 날짜가 둘 다 해당 월에 속하는 경우
            else
            {
                for(int i = appointment.getRentalDate().getDayOfMonth(); i <= appointment.getReturnDate().getDayOfMonth(); i++)
                {
                    map.put(LocalDate.of(year, month, i), false);
                }
            }
        }

        return GetItemAvailabilityRes.builder()
                .itemId(item.getId())
                .postId(rentalPost.getId())
                .year(year)
                .month(month)
                .availability(map)
                .build();
    }

    // 제품 구매가 가능한 첫 날짜 조회
    @Transactional(readOnly = true)
    public LocalDate getItemSaleAvailability(Long salePostId)
    {
        /// 판매 게시글 데이터 조회

        // 판매 게시글 데이터 조회
        Post salePost = postRepository.findById(salePostId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.POST_NOT_FOUND, salePostId));

        /// 예외 처리
        /// 1. 판매 게시글인가?

        // 판매 게시글이 아니라면 예외 처리
        if(salePost.getPostType() != PostType.SALE) {
            throw new CustomException(CustomExceptionCode.NOT_SALE_POST, salePost.getPostType());
        }

        /// 제품 데이터 조회

        // 제품 데이터 조회
        Item item = salePost.getItem();

        /// 예외 처리
        /// 1. 제품이 판매 예정이지 않은가?

        // 제품이 판매 예정이거나 판매 되었다면 예외 처리
        if(item.getSaleDate() != null) {
            throw new CustomException(CustomExceptionCode.ALREADY_SALE_RESERVED, item.getId());
        }

        /// 제품 구매가 가능한 첫 날짜 응답

        return getFirstSaleAvailableDate(item.getId());
    }

    // 약속 확정
    @Transactional
    public AppointmentRes confirmAppointment(User user, Long appointmentId)
    {
        /// 약속 데이터 조회

        // 약속 데이터 조회
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.APPOINTMENT_NOT_FOUND, appointmentId));

        /// 예외 처리
        /// 1. 약속이 제시 중인가?
        /// 2. 동의자와 약속 생성자가 다른 사용자인가?
        /// 3. 동의자가 약속 관계자인가?

        // 이미 확정된 약속인 경우, 예외 처리
        if(appointment.getState() != AppointmentState.PENDING) {
            throw new CustomException(CustomExceptionCode.NOT_PENDING_APPOINTMENT, null);
        }

        // 동의자와 약속 생성자가 동일한 경우, 예외 처리
        if(Objects.equals(user.getId(), appointment.getCreator().getId())) {
            throw new CustomException(CustomExceptionCode.CREATOR_CANNOT_AGREE, null);
        }

        // 동의자가 약속 관계자가 아닌 경우, 예외 처리
        if(!Objects.equals(user.getId(), appointment.getOwner().getId()) && !Objects.equals(user.getId(), appointment.getRequester().getId())) {
            throw new CustomException(CustomExceptionCode.NOT_APPOINTMENT_PARTICIPANT, null);
        }

        /// 게시글 데이터 조회

        // 게시글 데이터 조회
        Post post = appointment.getPost();

        /// 제품 데이터 조회

        // 제품 데이터 조회
        Item item = post.getItem();

        /// 대여 약속의 경우
            /// 1. 대여를 원하는 구간 동안 예정된 대여 약속이 하나도 존재하지 않는가?
            /// 2. 제품이 판매 예정이라면, 대여를 원하는 구간이 판매 날짜 이전인가?
        /// 구매 약속의 경우
            /// 1. 제품이 이미 판매 예정이지 않는가?
            /// 2. 구매를 원하는 날짜 이후에 대여 약속이 존재하지 않는가?
            /// + 제품의 판매 예정 날짜 변경

        // 대여 약속의 경우
        if(post.getPostType() == PostType.RENTAL)
        {
            // 대여를 원하는 구간 동안 예정된 대여 약속이 하나라도 존재하는 경우, 예외 처리
            if(!isPostAvailableOnInterval(post.getId(), appointment.getRentalDate(), appointment.getRentalDate())) {
                throw new CustomException(CustomExceptionCode.ALREADY_RENTAL_RESERVED_PERIOD, Map.of(
                        "rentalDate", appointment.getRentalDate(),
                        "returnDate", appointment.getReturnDate()
                ));
            }

            // 제품이 판매 예정 혹은 판매된 경우
            if(item.getSaleDate() != null)
            {
                // 대여를 원하는 구간이 판매 날짜 이전이 아닌 경우, 예외 처리
                if(!appointment.getReturnDate().isBefore(item.getSaleDate())) {
                    throw new CustomException(CustomExceptionCode.ALREADY_SALE_RESERVED_PERIOD, Map.of(
                            "rentalDate", appointment.getRentalDate(),
                            "returnDate", appointment.getReturnDate()
                    ));
                }
            }
        }
        // 구매 약속의 경우
        else
        {
            // 제품이 판매 예정이거나 판매 되었다면 예외 처리
            if(item.getSaleDate() != null) {
                throw new CustomException(CustomExceptionCode.ALREADY_SALE_RESERVED, item.getId());
            }

            // 제품 구매가 가능한 첫 날짜
            LocalDate firstSaleAvailableDate = getFirstSaleAvailableDate(item.getId());

            // 구매를 원하는 날짜가 구매가 가능한 첫 날짜 이전인 경우, 예외 처리
            if(firstSaleAvailableDate.isAfter(appointment.getRentalDate().toLocalDate())) {
                throw new CustomException(CustomExceptionCode.ALREADY_RENTAL_RESERVED_PERIOD, Map.of(
                        "requestSaleDate", appointment.getRentalDate(),
                        "firstAvailableSaleDate", firstSaleAvailableDate
                ));
            }

            // 제품의 판매 예정 날짜 변경
            item.confirmSale(LocalDateTime.now());
            itemRepository.save(item);
        }

        /// 약속 확정

        appointment.confirmAppointment();
        appointmentRepository.save(appointment);

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

        /// 예외 처리
        /// 1. 제시 중이거나 확정된 약속인가?
        /// 2. 취소자가 약속 관계자인가?

        // 제시 중이거나 확정된 약속이 아닌 경우, 예외 처리
        if(appointment.getState() != AppointmentState.PENDING && appointment.getState() != AppointmentState.CONFIRMED) {
            throw new CustomException(CustomExceptionCode.APPOINTMENT_CANNOT_CANCELLED, appointment.getState());
        }

        // 취소자가 약속 관계자가 아닌 경우, 예외 처리
        if(!Objects.equals(user.getId(), appointment.getOwner().getId()) && !Objects.equals(user.getId(), appointment.getRequester().getId())) {
            throw new CustomException(CustomExceptionCode.NOT_APPOINTMENT_PARTICIPANT, null);
        }

        /// 게시글 데이터 조회

        // 게시글 데이터 조회
        Post post = appointment.getPost();

        /// 제품 데이터 조회

        // 제품 데이터 조회
        Item item = post.getItem();

        /// 구매 약속의 경우, 제품의 판매 예정 날짜 변경

        // 구매 약속의 경우
        if(post.getPostType() == PostType.SALE)
        {
            // 제품의 판매 예정 날짜 변경
            item.cancelSale();
            itemRepository.save(item);
        }

        /// 약속 취소

        // 약속 취소
        appointment.cancelAppointment();
        appointmentRepository.save(appointment);

        // TODO: 사용자 피드백 요청
        // TODO: 채팅방 제거

        /// 약속 데이터 반환

        return AppointmentRes.from(appointment, post);
    }

    // (확정/대여중/완료) 상태의 나의 약속 페이지 조회
    @Transactional(readOnly = true)
    public AppointmentPageRes findMyAppointmentPage(User user,
                                                    Pageable pageable,
                                                    PostType type,
                                                    FindMyAppointmentSubject subject,
                                                    FindMyAppointmentPeriod period)
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

        /// 제품 ID, 대표 이미지 URL pair 정보 생성

        // Map(제품 id, 대표 이미지 URL) 생성
        Map<Long, String> thumbnailUrlMap = thumbnailList.stream()
                .collect(Collectors.toMap(
                        itemImage -> itemImage.getItem().getId(),
                        itemImage -> itemImageService.getFullImageUrl(itemImage)
                ));

        /// 약속, 대표 이미지 URL pair 정보 생성

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

        /// 예외 처리
        /// 1. 확정된 약속인가?
        /// 2. 요청을 보낸 사용자가 약속 관계자인가?

        // 확정된 약속이 아닌 경우, 예외 처리
        if(appointment.getState() != AppointmentState.CONFIRMED) {
            throw new CustomException(CustomExceptionCode.NOT_CONFIRMED_APPOINTMENT, appointment.getState());
        }

        // 요청을 보낸 사용자가 약속 관계자가 아닌 경우, 예외 처리
        if(!Objects.equals(user.getId(), appointment.getOwner().getId()) && !Objects.equals(user.getId(), appointment.getRequester().getId())) {
            throw new CustomException(CustomExceptionCode.NOT_APPOINTMENT_PARTICIPANT, null);
        }

        /// 게시글 데이터 조회

        Post post = appointment.getPost();

        /// 제품 데이터 조회

        Item item = post.getItem();

        /// 구매 약속의 경우, 제품의 판매 예정 날짜 변경

        // 구매 약속의 경우
        if(post.getPostType() == PostType.SALE)
        {
            // 제품의 판매 예정 날짜 변경
            item.cancelSale();
            itemRepository.save(item);
        }

        /// 기존 약속 데이터 취소

        appointment.cancelAppointment();

        /// 예외 처리
        /// 가격 협의가 불가능한데, 가격을 바꿔서 요청하지는 않았는가?

        // 가격 협의가 불가능한데 가격을 바꿔서 요청을 보내는 경우, 예외 처리
        if(!item.getCanDeal() && (dto.getPrice() != post.getPrice() || dto.getDeposit() != post.getDeposit())) {
            throw new CustomException(CustomExceptionCode.DEAL_NOT_ALLOWED, null);
        }

        /// 예외 처리
        /// 대여 게시글의 경우
            /// 1. 반납 일시가 대여 일시 이후인가?
            /// 2. 대여를 원하는 구간 동안, 예정된 대여 약속이 하나도 존재하지 않는가?
            /// 3. 대여를 원하는 구간이 제품 판매 날짜 이전인가?
        /// 판매 게시글의 경우
            /// 1. 구매를 원하는 날짜 이후에 대여 약속이 존재하지 않는가?

        if(post.getPostType() == PostType.RENTAL)
        {
            // 1. 반납 일시가 대여 일시의 이후가 아닌 경우, 예외 처리
            if(!dto.getReturnDate().isAfter(dto.getRentalDate())) {
                throw new CustomException(CustomExceptionCode.RENTAL_DATE_IS_LATER_THAN_RETURN_DATE, Map.of(
                        "rentalDate", dto.getRentalDate(),
                        "returnDate", dto.getReturnDate()
                ));
            }

            // 2. 대여를 원하는 구간 동안 예정된 대여 약속이 하나라도 존재하는 경우, 예외 처리
            if(!isPostAvailableOnInterval(post.getId(), dto.getRentalDate(), dto.getReturnDate())) {
                throw new CustomException(CustomExceptionCode.ALREADY_RENTAL_RESERVED_PERIOD, Map.of(
                        "rentalDate", dto.getRentalDate(),
                        "returnDate", dto.getReturnDate()
                ));
            }

            // 제품이 판매 예정 혹은 판매된 경우
            if(item.getSaleDate() != null)
            {
                // 3. 대여를 원하는 구간이 판매 날짜 이후인 경우, 예외 처리
                if(!dto.getReturnDate().isBefore(item.getSaleDate())) {
                    throw new CustomException(CustomExceptionCode.ALREADY_SALE_RESERVED_PERIOD, Map.of(
                            "rentalDate", dto.getRentalDate(),
                            "returnDate", dto.getReturnDate()
                    ));
                }
            }
        }
        else
        {
            // 제품 구매가 가능한 첫 날짜
            LocalDate firstSaleAvailableDate = getFirstSaleAvailableDate(item.getId());

            // 1. 구매를 원하는 날짜가 구매가 가능한 첫 날짜 이전인 경우, 예외 처리
            if(firstSaleAvailableDate.isAfter(dto.getRentalDate().toLocalDate())) {
                throw new CustomException(CustomExceptionCode.ALREADY_RENTAL_RESERVED_PERIOD, Map.of(
                        "requestSaleDate", dto.getRentalDate(),
                        "firstAvailableSaleDate", firstSaleAvailableDate
                ));
            }
        }

        /// 새로운 약속 데이터 생성

        // 새로운 약속 데이터 생성
        Appointment newAppointment = appointment.clone(user);
        newAppointment.updateAppointment(dto, post.getPostType() == PostType.RENTAL);

        // 약속 데이터 저장
        appointmentRepository.save(newAppointment);

        /// 약속 데이터 반환

        return AppointmentRes.from(newAppointment, post);
    }

    // 대여 중인 약속 변경 제시
    @Transactional
    public AppointmentRes offerToUpdateInProgressAppointment(User user, OfferToUpdateInProgressAppointmentReq dto)
    {
        /// 약속 데이터 조회

        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.APPOINTMENT_NOT_FOUND, dto.getAppointmentId()));

        /// 예외 처리
        /// 1. 대여 중인 약속인가?
        /// 2. 요청을 보낸 사용자가 약속 관계자인가?

        // 대여 중인 약속이 아닌 경우, 예외 처리
        if(appointment.getState() != AppointmentState.IN_PROGRESS) {
            throw new CustomException(CustomExceptionCode.NOT_IN_PROGRESS_APPOINTMENT, appointment.getState());
        }

        // 요청을 보낸 사용자가 약속 관계자가 아닌 경우, 예외 처리
        if(!Objects.equals(user.getId(), appointment.getOwner().getId()) && !Objects.equals(user.getId(), appointment.getRequester().getId())) {
            throw new CustomException(CustomExceptionCode.NOT_APPOINTMENT_PARTICIPANT, null);
        }

        /// 게시글 데이터 조회

        Post post = appointment.getPost();

        /// 제품 데이터 조회

        Item item = post.getItem();

        /// 예외 처리
        /// 1. 반납 일시가 현재 이후인가?
        /// 2. 반납 일시까지 예정된 대여 약속이 하나도 존재하지 않는가?
        /// 3. 반납 일시가 제품 판매 날짜 이전인가?

        // 1. 반납 일시가 현재 이전인 경우, 예외 처리
        if(!dto.getReturnDate().isAfter(LocalDateTime.now())) {
            throw new CustomException(CustomExceptionCode.CURRENT_DATE_IS_LATER_THAN_RETURN_DATE, dto.getReturnDate());
        }

        // 2. 반납 일시까지 예정된 대여 약속이 하나라도 존재하는 경우, 예외 처리
        if(!isPostAvailableOnInterval(post.getId(), appointment.getReturnDate(), dto.getReturnDate())) {
            throw new CustomException(CustomExceptionCode.ALREADY_RENTAL_RESERVED_PERIOD, dto.getReturnDate());
        }

        // 제품이 판매 예정 혹은 판매된 경우
        if(item.getSaleDate() != null)
        {
            // 3. 반납 일시가 제품 판매 날짜 이후인 경우, 예외 처리
            if(!dto.getReturnDate().isBefore(item.getSaleDate())) {
                throw new CustomException(CustomExceptionCode.ALREADY_SALE_RESERVED_PERIOD, dto.getReturnDate());
            }
        }

        // TODO: 대여 중인 약속 변경 요청을 저장해야 함.

        /// 약속 데이터 반환

        // 반환할 약속 데이터 생성
        // * 확정된 변경 요청이 아니므로 저장하지 않습니다!
        Appointment newAppointment = appointment.clone(user);
        newAppointment.updateAppointment(dto);

        // 약속 데이터 반환
        return AppointmentRes.from(newAppointment, post);
    }

    /// 공통 로직

    // 해당 날짜에 예정된 대여 약속이 하나도 없는지 판별
    public boolean isPostAvailableOnDate(Long postId, LocalDateTime date) {
        return appointmentRepository.findListOverlappingWithPeriod(
                postId,
                AppointmentState.CONFIRMED,
                date,
                date
        ).isEmpty();
    }

    // 해당 구간 동안 예정된 대여 약속이 하나도 존재하지 않는지 판별
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

    // 제품 구매가 가능한 첫 날짜 조회
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
}
