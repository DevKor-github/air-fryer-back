package com.airfryer.repicka.domain.appointment.service;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.util.validation.AppointmentValidator;
import com.airfryer.repicka.domain.appointment.dto.GetItemAvailabilityRes;
import com.airfryer.repicka.domain.appointment.dto.OfferAppointmentInPostReq;
import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
import com.airfryer.repicka.domain.item.entity.CurrentItemState;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.post.entity.PostType;
import com.airfryer.repicka.domain.post.repository.PostRepository;
import com.airfryer.repicka.domain.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AppointmentService
{
    private final AppointmentRepository appointmentRepository;
    private final PostRepository postRepository;

    private final AppointmentValidator appointmentValidator;

    // 게시글에서 약속 제시
    @Transactional
    public void offerAppointmentInPost(User borrower, OfferAppointmentInPostReq dto)
    {
        // 게시글 데이터 조회
        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.POST_NOT_FOUND, dto.getPostId()));

        // 대여의 경우 예외 처리
        // 1. 반납 일시 데이터를 전달하지 않았다면 예외 처리
        // 2. 반납 일시가 대여 일시의 이후인지 확인
        if(post.getPostType() == PostType.RENTAL)
        {
            if(dto.getReturnDate() == null) {
                throw new CustomException(CustomExceptionCode.RETURN_DATE_NOT_FOUND, null);
            } else if(!appointmentValidator.isRentalDateEarlierThanReturnDate(dto.getRentalDate(), dto.getReturnDate())) {
                throw new CustomException(CustomExceptionCode.RENTAL_DATE_IS_LATER_THAN_RETURN_DATE, Map.of(
                        "rentalDate", dto.getRentalDate(),
                        "returnDate", dto.getReturnDate()
                ));
            }
        }

        // 소유자와 대여자가 다른지 확인
        if(!appointmentValidator.isOwnerAndBorrowerDifferent(post.getWriter(), borrower)) {
            throw new CustomException(CustomExceptionCode.SAME_OWNER_AND_BORROWER, borrower.getId());
        }

        // 가격 협의가 불가능한데 가격을 바꿔서 요청을 보내는 경우, 예외 처리
        if(!post.getItem().getCanDeal() && (dto.getPrice() != post.getPrice() || dto.getDeposit() != post.getDeposit())) {
            throw new CustomException(CustomExceptionCode.DEAL_NOT_ALLOWED, null);
        }

        // TODO: 기존에 확정된 다른 약속 데이터랑 충돌하지 않는지 확인하는 로직이 필요함.

        // 협의 중인 약속 데이터가 이미 존재한다면, 기존 데이터를 수정
        // 협의 중인 약속 데이터가 존재하지 않는다면, 새로운 데이터를 생성
        Optional<Appointment> pendingAppointmentOptional = appointmentRepository.findByPostIdAndOwnerAndBorrowerAndState(
                post.getId(),
                post.getWriter(),
                borrower,
                AppointmentState.PENDING
        );

        if(pendingAppointmentOptional.isPresent())
        {
            // 기존에 존재하던 약속 데이터
            Appointment pendingAppointment = pendingAppointmentOptional.get();

            // 약속 데이터 수정
            pendingAppointment.updateAppointment(dto, post.getPostType() == PostType.RENTAL);

            // 약속 데이터 저장
            appointmentRepository.save(pendingAppointment);
        }
        else
        {
            // 새로운 약속 데이터 생성
            Appointment appointment = Appointment.builder()
                    .post(post)
                    .creator(borrower)
                    .owner(post.getWriter())
                    .borrower(borrower)
                    .rentalLocation(dto.getRentalLocation().trim())
                    .returnLocation(post.getPostType() == PostType.RENTAL ? dto.getReturnLocation().trim() : null)
                    .rentalDate(dto.getRentalDate())
                    .returnDate(post.getPostType() == PostType.RENTAL ? dto.getReturnDate() : null)
                    .price(dto.getPrice())
                    .deposit(dto.getDeposit())
                    .state(AppointmentState.PENDING)
                    .build();

            // 약속 데이터 저장
            appointmentRepository.save(appointment);
        }

        // TODO: 채팅방 데이터 반환해야 함.
    }

    // 월 단위로 날짜별 제품 대여 가능 여부 조회
    public GetItemAvailabilityRes getItemRentalAvailability(Long rentalPostId, int year, int month)
    {
        // 대여 게시글 데이터 조회
        Post rentalPost = postRepository.findById(rentalPostId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.POST_NOT_FOUND, rentalPostId));

        // 대여 게시글이 아니라면 예외 처리
        if (rentalPost.getPostType() != PostType.RENTAL) {
            throw new CustomException(CustomExceptionCode.NOT_RENTAL_POST, rentalPostId);
        }

        // 반환할 날짜별 제품 대여 가능 여부 해시맵
        Map<LocalDate, Boolean> map = new LinkedHashMap<>();

        // 일단, 모든 날짜를 true로 초기화
        for (int i = 1; i <= YearMonth.of(year, month).lengthOfMonth(); i++) {
            map.put(LocalDate.of(year, month, i), true);
        }

        // 이전의 날짜들은 전부 불가능 처리
        for (int i = 1; i <= YearMonth.of(year, month).lengthOfMonth() && LocalDate.of(year, month, i).isBefore(LocalDate.now()); i++) {
            map.put(LocalDate.of(year, month, i), false);
        }

        /// 판매 게시글이 존재한다면, 예정된 판매 약속의 존재 여부 확인
        /// 예정된 판매 약속이 존재한다면, 해당 약속 이후 날짜는 전부 대여 불가능

        // 제품 조회
        Item item = rentalPost.getItem();

        // 동일한 제품에 대한 판매 게시글 조회
        Optional<Post> salePostOptional = postRepository.findByItemIdAndPostType(item.getId(), PostType.SALE);

        // 판매 게시글이 존재할 때
        if (salePostOptional.isPresent()) {
            Post salePost = salePostOptional.get();

            // 예정된 판매 약속 조회
            Optional<Appointment> saleAppointmentOptional = appointmentRepository.findByPostIdAndReturnDateAndState(
                    salePost.getId(),
                    null,
                    AppointmentState.CONFIRMED
            );

            // 예정된 판매 약속이 존재한다면, 해당 약속 이후 날짜는 전부 대여 불가능
            if (saleAppointmentOptional.isPresent()) {
                Appointment saleAppointment = saleAppointmentOptional.get();

                for (int i = saleAppointment.getRentalDate().getDayOfMonth(); i <= YearMonth.of(year, month).lengthOfMonth(); i++) {
                    map.put(LocalDate.of(year, month, i), false);
                }
            }
        }

        /// 해당 월 동안 예정된 대여 약속들에 대해, 각 구간마다 대여 불가능 처리

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
                for(int i = appointment.getRentalDate().getDayOfMonth(); i <= YearMonth.of(year, month).lengthOfMonth(); i++)
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

    // 월 단위로 날짜별 제품 대여 가능 여부 조회
    public LocalDate getItemSaleAvailability(Long salePostId)
    {
        // 판매 게시글 데이터 조회
        Post salePost = postRepository.findById(salePostId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.POST_NOT_FOUND, salePostId));

        // 판매 게시글이 아니라면 예외 처리
        if(salePost.getPostType() != PostType.SALE) {
            throw new CustomException(CustomExceptionCode.NOT_SALE_POST, salePostId);
        }

        // 제품 조회
        Item item = salePost.getItem();

        // 제품이 판매 예정이면 예외 처리
        if(item.getState() == CurrentItemState.SALE_RESERVED) {
            throw new CustomException(CustomExceptionCode.ALREADY_SALE_RESERVED, item.getId());
        }

        // 제품 구매가 가능한 첫 날짜
        LocalDate result = LocalDate.now();

        /// 대여 게시글이 존재한다면, 예정된 모든 대여 약속 조회
        /// 모든 대여 약속 중 가장 나중의 반납 날짜 다음날을 반환하도록 처리

        // 해당 제품에 대한 대여 게시글 조회
        Optional<Post> rentalPostOptional = postRepository.findByItemIdAndPostType(item.getId(), PostType.RENTAL);

        // 대여 게시글이 존재할 때
        if(rentalPostOptional.isPresent())
        {
            Post rentalPost = rentalPostOptional.get();

            // 모든 예정된 대여 약속 조회
            List<Appointment> appointmentList = appointmentRepository.findByPostIdAndState(
                    rentalPost.getId(),
                    AppointmentState.CONFIRMED
            );

            // 모든 대여 약속에 대해, 대여 반납 날짜의 다음날보다 제품 구매가 가능한 첫 날짜가 이전이라면 갱신
            for (Appointment appointment : appointmentList)
            {
                if(result.isBefore(appointment.getReturnDate().toLocalDate().plusDays(1))) {
                    result = appointment.getReturnDate().toLocalDate().plusDays(1);
                }
            }
        }

        return result;
    }
}
