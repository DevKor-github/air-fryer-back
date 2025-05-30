package com.airfryer.repicka.domain.appointment.service;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.util.validation.AppointmentValidator;
import com.airfryer.repicka.domain.appointment.dto.OfferAppointmentInPostReq;
import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.post.entity.PostType;
import com.airfryer.repicka.domain.post.repository.PostRepository;
import com.airfryer.repicka.domain.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

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
        Optional<Appointment> pendingAppointmentOptional = appointmentRepository.findByPostAndOwnerAndBorrowerAndState(
                post,
                post.getWriter(),
                borrower,
                AppointmentState.PENDING
        );

        if(pendingAppointmentOptional.isPresent())
        {
            // 기존에 존재하던 약속 데이터
            Appointment pendingAppointment = pendingAppointmentOptional.get();

            // 약속 데이터 수정
            pendingAppointment.updateAppointment(dto);

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
}
