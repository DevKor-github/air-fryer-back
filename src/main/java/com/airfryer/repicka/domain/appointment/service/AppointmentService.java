package com.airfryer.repicka.domain.appointment.service;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.util.validation.AppointmentValidator;
import com.airfryer.repicka.domain.appointment.dto.CreateAppointmentInPostRequestDto;
import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.post.repository.PostRepository;
import com.airfryer.repicka.domain.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppointmentService
{
    private final AppointmentRepository appointmentRepository;
    private final PostRepository postRepository;

    private final AppointmentValidator appointmentValidator;

    // 대여 신청을 통한 약속 생성
    // 빌리고 싶은 사람이 게시글에서 바로 대여 신청 버튼을 눌러서 약속을 생성하는 방식
    @Transactional
    public void createAppointmentInPost(User borrower, Long postId, CreateAppointmentInPostRequestDto dto)
    {
        // TODO: 채팅방 및 약속이 이미 생성된 상태라면 예외 처리 해야함.

        // 게시글 데이터 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.POST_NOT_FOUND, postId));

        // 반납 일사가 대여 일시의 이후인지 확인
        if(!appointmentValidator.isRentalDateEarlierThanReturnDate(dto.getRentalDate(), dto.getReturnDate())) {
            throw new CustomException(CustomExceptionCode.RENTAL_DATE_IS_LATER_THAN_RETURN_DATE, Map.of(
                    "rentalDate", dto.getRentalDate(),
                    "returnDate", dto.getReturnDate()
            ));
        }

        // 소유자와 대여자가 다른지 확인
        if(!appointmentValidator.isOwnerAndBorrowerDifferent(post.getWriter(), borrower)) {
            throw new CustomException(CustomExceptionCode.SAME_OWNER_AND_BORROWER, borrower.getId());
        }

        // 약속 데이터 생성
        Appointment appointment = Appointment.builder()
                .post(post)
                .creator(borrower)
                .owner(post.getWriter())
                .borrower(borrower)
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

        // TODO: 채팅방 생성해서 데이터 반환해야 함.
    }
}
