package com.airfryer.repicka.domain.review;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import com.airfryer.repicka.domain.review.dto.ReviewReq;
import com.airfryer.repicka.domain.review.entity.Review;
import com.airfryer.repicka.domain.review.repository.ReviewRepository;
import com.airfryer.repicka.domain.appointment.entity.Appointment;
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
        Appointment appointment = appointmentRepository.findByIdAndUserId(reviewReq.getAppointmentId(), userId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.APPOINTMENT_NOT_FOUND, null));

        if (reviewRepository.existsByAppointmentIdAndReviewerId(reviewReq.getAppointmentId(), userId)) {
            throw new CustomException(CustomExceptionCode.REVIEW_ALREADY_EXISTS, null);
        }

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
