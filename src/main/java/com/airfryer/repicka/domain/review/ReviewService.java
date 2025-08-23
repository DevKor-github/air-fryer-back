package com.airfryer.repicka.domain.review;

import java.time.LocalDate;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import com.airfryer.repicka.domain.review.dto.ReviewReq;
import com.airfryer.repicka.domain.review.entity.Review;
import com.airfryer.repicka.domain.review.repository.ReviewRepository;
import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.appointment.entity.AppointmentType;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;

    // 리뷰 생성
    @Transactional
    public void createReview(ReviewReq reviewReq, Long userId) {
        // 본인이 참여한 약속 조회
        Appointment appointment = appointmentRepository.findByIdAndUserId(reviewReq.getAppointmentId(), userId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.APPOINTMENT_NOT_FOUND, null));

        // SUCCESS가 아니면 약속 타입에 따른 날짜 확인 및 상태 변경
        LocalDate today = LocalDate.now();
        if (appointment.getState() != AppointmentState.SUCCESS) {
            if (appointment.getType() == AppointmentType.RENTAL && today.equals(appointment.getReturnDate().toLocalDate())) {
                // 대여 약속이면 반납 날짜가 오늘인지 확인
                appointment.success();
            } 
            else if (appointment.getType() == AppointmentType.SALE && today.equals(appointment.getRentalDate().toLocalDate())) {
                // 구매 약속이면 구매 날짜가 오늘인지 확인
                appointment.success();
            }
            else {  // 약속 타입에 따른 날짜가 오늘이 아니면 예외 발생
                throw new CustomException(CustomExceptionCode.APPOINTMENT_NOT_SUCCESS, null);
            }
        }

        // 리뷰가 이미 존재하면 예외 발생
        if (reviewRepository.existsByAppointmentIdAndReviewerId(reviewReq.getAppointmentId(), userId)) {
            throw new CustomException(CustomExceptionCode.REVIEW_ALREADY_EXISTS, null);
        }

        // 리뷰 생성
        Review review = Review.builder()
            .appointment(appointment)
            .rating(reviewReq.getRating())
            .content(reviewReq.getContent())
            .reviewer(appointment.getOwner().getId() == userId ? appointment.getOwner() : appointment.getRequester())
            .reviewed(appointment.getOwner().getId() == userId ? appointment.getRequester() : appointment.getOwner())
            .build();

        reviewRepository.save(review);
    }

    // 리뷰 조회
    @Transactional(readOnly = true)
    public List<Review> getReview(Long userId) {
        return reviewRepository.findByReviewedId(userId);
    }
}
